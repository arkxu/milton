/* FileMaker.java

FileMaker: File reading and making class
Copyright (C) 2011 Tomáš Hlavnička <hlavntom@fel.cvut.cz>

This file is a part of Jazsync.

Jazsync is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the
Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

Jazsync is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Jazsync; if not, write to the

Free Software Foundation, Inc.,
59 Temple Place, Suite 330,
Boston, MA  02111-1307
USA
 */
package com.ettrema.zsync;

import com.bradmcevoy.io.StreamUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;

/**
 *
 * @author brad, original work by Tomáš Hlavnička
 */
public class MapMatcher {
	
    private Generator gen = new Generator();	
	
    /**
     * Reads file and map it's data into the fileMap.
     */
    public double mapMatcher(File inputFile, MetaFileReader mfr, MakeContext mc) {
        int bufferOffset = 0;
		InputStream is = null;
		long fileLength = inputFile.length();
        try {
			is = new FileInputStream(inputFile);
			InputStream inBuf = new BufferedInputStream(is);
            Security.addProvider(new JarsyncProvider());
            Configuration config = new Configuration();
            config.strongSum = MessageDigest.getInstance("MD4");
            config.weakSum = new Rsum();
            config.blockLength = mfr.getBlocksize();
            config.strongSumLength = mfr.getChecksumBytes();
            int weakSum;
            byte[] strongSum;
            byte[] backBuffer = new byte[mfr.getBlocksize()];
            byte[] blockBuffer = new byte[mfr.getBlocksize()];
            byte[] fileBuffer;
            int mebiByte = 1048576;
            if (mfr.getLength() < mebiByte && mfr.getBlocksize() < mfr.getLength()) {
                fileBuffer = new byte[(int) mfr.getLength()];
            } else if (mfr.getBlocksize() > mfr.getLength() || mfr.getBlocksize() > mebiByte) {
                fileBuffer = new byte[mfr.getBlocksize()];
            } else {
                fileBuffer = new byte[mebiByte];
            }
            int n;
            byte newByte;
            boolean firstBlock = true;
            int len = fileBuffer.length;
            boolean end = false;
            double a = 10;
            int blocksize = mfr.getBlocksize();
            while (mc.fileOffset != fileLength) {
                n = inBuf.read(fileBuffer, 0, len);
                if (firstBlock) {
                    weakSum = gen.generateWeakSum(fileBuffer, 0, config);
                    bufferOffset = mfr.getBlocksize();
                    int weak = updateWeakSum(weakSum, mfr);
                    if (hashLookUp(weak, null, blocksize, mc)) {
                        strongSum = gen.generateStrongSum(fileBuffer, 0, blocksize, config);
                        hashLookUp(updateWeakSum(weakSum, mfr), strongSum, blocksize, mc);
                    }
                    mc.fileOffset++;
                    firstBlock = false;
                }
                for (; bufferOffset < fileBuffer.length; bufferOffset++) {
                    newByte = fileBuffer[bufferOffset];
                    if (mc.fileOffset + mfr.getBlocksize() > fileLength) {
                        newByte = 0;
                    }
                    weakSum = gen.generateRollSum(newByte, config);
                    if (hashLookUp(updateWeakSum(weakSum, mfr), null, blocksize, mc)) {
                        if (mc.fileOffset + mfr.getBlocksize() > fileLength) {
                            if (n > 0) {
                                Arrays.fill(fileBuffer, n, fileBuffer.length, (byte) 0);
                            } else {
                                int offset = fileBuffer.length - mfr.getBlocksize() + bufferOffset + 1;
                                System.arraycopy(fileBuffer, offset, blockBuffer, 0, fileBuffer.length - offset);
                                Arrays.fill(blockBuffer, fileBuffer.length - offset, blockBuffer.length, (byte) 0);
                            }
                        }
                        if ((bufferOffset - mfr.getBlocksize() + 1) < 0) {
                            if (n > 0) {
                                System.arraycopy(backBuffer, backBuffer.length + bufferOffset - mfr.getBlocksize() + 1, blockBuffer, 0, mfr.getBlocksize() - bufferOffset - 1);
                                System.arraycopy(fileBuffer, 0, blockBuffer, mfr.getBlocksize() - bufferOffset - 1, bufferOffset + 1);
                            }
                            strongSum = gen.generateStrongSum(blockBuffer, 0, blocksize, config);
                            hashLookUp(updateWeakSum(weakSum, mfr), strongSum, blocksize, mc);
                        } else {
                            strongSum = gen.generateStrongSum(fileBuffer, bufferOffset - blocksize + 1, blocksize, config);
                            hashLookUp(updateWeakSum(weakSum, mfr), strongSum, blocksize, mc);
                        }
                    }
                    mc.fileOffset++;
                    if ((((double) mc.fileOffset / (double) fileLength) * 100) >= a) {
                        progressBar(((double) mc.fileOffset / (double) fileLength) * 100);
                        a += 10;
                    }
                    if (mc.fileOffset == fileLength) {
                        end = true;
                        break;
                    }
                }
                System.arraycopy(fileBuffer, fileBuffer.length - mfr.getBlocksize(), backBuffer, 0, mfr.getBlocksize());
                bufferOffset = 0;
                if (end) {
                    break;
                }
            }

            System.out.println();
            double complete = matchControl(mfr, mc);
            mc.fileMap[mc.fileMap.length - 1] = -1;
            is.close();
            return complete;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("Problem with MD4 checksum");
            throw new RuntimeException(ex);
        } finally {
			StreamUtils.close(is);
		}
    }
	

    /**
     * Shorten the calculated weakSum according to variable length of weaksum
     * @param weak Generated full weakSum
     * @return Shortened weakSum
     */
    private int updateWeakSum(int weak, MetaFileReader mfr) {
        byte[] rsum;
        switch (mfr.getRsumBytes()) {
            case 2:
                rsum = new byte[]{(byte) 0,
                    (byte) 0,
                    (byte) (weak >> 24), //1
                    (byte) ((weak << 8) >> 24) //2
                };
                break;
            case 3:
                rsum = new byte[]{(byte) ((weak << 8) >> 24), //2
                    (byte) 0, //3
                    (byte) ((weak << 24) >> 24), //0
                    (byte) (weak >> 24) //1
                };
                break;
            case 4:
                rsum = new byte[]{(byte) (weak >> 24), //1
                    (byte) ((weak << 8) >> 24), //2
                    (byte) ((weak << 16) >> 24), //3
                    (byte) ((weak << 24) >> 24) //0
                };
                break;
            default:
                rsum = new byte[4];
        }
        int weakSum = 0;
        weakSum += (rsum[0] & 0x000000FF) << 24;
        weakSum += (rsum[1] & 0x000000FF) << 16;
        weakSum += (rsum[2] & 0x000000FF) << 8;
        weakSum += (rsum[3] & 0x000000FF);
        return weakSum;
    }	
	

    /**
     * Looks into hash table and check if got a hit
     * @param weakSum Weak rolling checksum
     * @param strongSum Strong MD4 checksum
     * @return True if we got a hit
     */
    private boolean hashLookUp(int weakSum, byte[] strongSum, int blocksize, MakeContext mc) {
        ChecksumPair p;
        if (strongSum == null) {
            p = new ChecksumPair(weakSum);
            ChecksumPair link = mc.hashtable.find(p);
            if (link != null) {
                return true;
            }
        } else {
            p = new ChecksumPair(weakSum, strongSum);
            ChecksumPair link = mc.hashtable.findMatch(p);
            int seq;
            if (link != null) {
                /** V pripade, ze nalezneme shodu si zapiseme do file mapy offset
                 * bloku, kde muzeme dana data ziskat.
                 * Nasledne po sobe muzeme tento zaznam z hash tabulky vymazat.
                 */
                seq = link.getSequence();
                mc.fileMap[seq] = mc.fileOffset;
                mc.hashtable.delete(new ChecksumPair(weakSum, strongSum, blocksize * seq, blocksize, seq));
                return true;
            }
        }
        return false;
    }	
	
    /**
     * Method is used to draw a progress bar of
     * how far we are in file.
     * @param i How much data we already progressed (value in percents)
     */
    private void progressBar(double i) {
        System.out.println("progress: " + i + "%");
    }
	
    /**
     * Clears non-matching blocks and returns percentage
     * value of how complete is our file
     * @return How many percent of file we have already
     */
    private double matchControl(MetaFileReader mfr, MakeContext mc) {
        int missing = 0;
        long[] fileMap = mc.fileMap;
        for (int i = 0; i < fileMap.length; i++) {
            if (mfr.getSeqNum() == 2) { //pouze pokud kontrolujeme matching continuation
                if (i > 0 && i < fileMap.length - 1) {
                    if (fileMap[i - 1] == -1 && fileMap[i] != -1 && fileMap[i + 1] == -1) {
                        fileMap[i] = -1;
                    }
                } else if (i == 0) {
                    if (fileMap[i] != -1 && fileMap[i + 1] == -1) {
                        fileMap[i] = -1;
                    }
                } else if (i == fileMap.length - 1) {
                    if (fileMap[i] != -1 && fileMap[i - 1] == -1) {
                        fileMap[i] = -1;
                    }
                }
            }
            if (fileMap[i] == -1) {
                missing++;
            }
        }
        System.out.println("matchControl: fileMap.length: " + fileMap.length + " - missing: " + missing);
        return ((((double) fileMap.length - missing) / (double) fileMap.length) * 100);
    }
	
}

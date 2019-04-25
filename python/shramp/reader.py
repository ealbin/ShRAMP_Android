#!/usr/bin/env python3

"""Functions to read ShRAMP-generated data files
"""

import numpy as np
import struct

__author__  = 'Eric Albin'
__email__   = 'Eric.K.Albin@gmail.com'
__updated__ = '24 April 2019'


def image(filename, reshape=False):
    """Reads in a ShRAMP-generated file with extension .frame
    
       Parameters
       ----------
       
          filename : String
                     A file path to the file you want to read in
       
          reshape : True or False
                    When False (default) return pixel values as a 1-D array, npixels long.
                    When True, return pixel values as a 2-D array, shape = (rows x columns).
                    In this latter case, (0,0) cooresponds to the upper left corner of the image.
                       
       Returns
       -------
           
           out : B, R, C, E, T, V
                 A tuple in this order: (B) bits-per-pixel (8 = YUV, 16 = RAW), 
                                        (R) number of pixel rows,
                                        (C) number of pixel columns,
                                        (E) sensor exposure in nanoseconds (if available, otherwise 0)
                                        (T) battery temperature when this was made in Celsius
                                        (V) np.array() of length n-pixels = (R)x(C) of pixel values

        See Also
        --------
        mask : read in .mask files (pixel mask)                                        
        statistic : read in .mean/.stddev/.stderr/.signif files (pixel statistics)
                                    
       Example
       -------
       >>> B, R, C, E, T, V = shramp.reader.image('foo/bar/filename.frame', reshape=True)
           
    """
    if ( not ( filename.endswith('.frame') ) ):
        print('Incorrect file extension: "' + filename.split('.')[-1] + '", cannot open with this function')
        return;
        
    with open(filename, 'rb') as file:
        bits    = int.from_bytes(file.read(1), byteorder='big')
        rows    = int.from_bytes(file.read(4), byteorder='big')
        cols    = int.from_bytes(file.read(4), byteorder='big')
        expo    = int.from_bytes(file.read(8), byteorder='big')
        temp    = struct.unpack('>f', file.read(4))[0]
        npixels = rows * cols
        if (bits == 8):
            values = np.asarray( struct.unpack('>' + 'b'*npixels, file.read(npixels)) )
        elif (bits == 16):
            values = np.asarray( struct.unpack('>' + 'h'*npixels, file.read(2*npixels)) )
        else:
            print('Unexpected image format, pixel depth is: ' + str(bits) + ', cannot read at this time')
            return;    
        if (reshape):
            values = values.reshape(rows, cols)
        return bits, rows, cols, expo, temp, values
        
        
def mask(filename, reshape=False):
    """Reads in a ShRAMP-generated file with extension .mask
       
       Parameters
       ----------
                  
           filename :  String
                       A file path to the file you want to read in
       
           reshape : True or False
                     When False (default) return pixel values as a 1-D array, npixels long.
                     When True, return pixel values as a 2-D array, shape = (rows x columns).
                     In this latter case, (0,0) cooresponds to the upper left corner of the image.       
       
       Returns
       -------
       
           out : B, R, C, M
                 A tuple in this order: (B) bits-per-pixel (8 = YUV, 16 = RAW), 
                                        (R) number of pixel rows,
                                        (C) number of pixel columns,
                                        (M) np.array() of length n-pixels = (R)x(C) of mask values
        See Also
        --------
        image : read in .frame files (images)
        statistic : read in .mean/.stddev/.stderr/.signif files (pixel statistics)
        
        Example 
        -------
        >>> B, R, C, M = shramp.reader.mask('foo/bar/filename.mask', reshape=True)
    """
    if ( not ( filename.endswith('.mask') ) ):
        print('Incorrect file extension: "' + filename.split('.')[-1] + '", cannot open with this function')
        return;
        
    with open(filename, 'rb') as file:
        bits    = int.from_bytes(file.read(1), byteorder='big')
        rows    = int.from_bytes(file.read(4), byteorder='big')
        cols    = int.from_bytes(file.read(4), byteorder='big')
        npixels = rows * cols
        mask    = np.asarray( struct.unpack('>' + 'b'*npixels, file.read(npixels)) )
        if (reshape):
            mask = mask.reshape(rows, cols)
        return bits, rows, cols, mask
        
       
def statistic(filename, reshape=False):
    """Reads in a ShRAMP-generated file with extension .mean, .stddev, .stderr or .signif
       
       Parameters
       ----------
       
           filename : String
                      A file path to the file you want to read in
       
           reshape : True or False
                     When False (default) return pixel values as a 1-D array, npixels long.
                     When True, return pixel values as a 2-D array, shape = (rows x columns).
                     In this latter case, (0,0) cooresponds to the upper left corner of the image.       
       
       Returns
       -------
       
           out : B, R, C, F, T, S
                 A tuple in this order: (B) bits-per-pixel (8 = YUV, 16 = RAW), 
                                        (R) number of pixel rows,
                                        (C) number of pixel columns,
                                        (F) number of image frames that went into this statistic,
                                        (T) battery temperature when this was made in Celsius
                                        (S) np.array() of length n-pixels = (R)x(C) of statistic values
                                        
        See Also
        --------
        image : read in .frame files (images)
        mask : read in .mask files (pixel mask)
        
        Example 
        -------
        >>> B, R, C, F, T, S = shramp.reader.statistic('foo/bar/filename.stddev', reshape=True)                                        
    """
    if ( not ( filename.endswith('.mean') or filename.endswith('.stddev')
               or filename.endswith('.stderr') or filename.endswith('.signif') ) ):
        print('Incorrect file extension: "' + filename.split('.')[-1] + '", cannot open with this function')
        return;
        
    with open(filename, 'rb') as file:
        bits    = int.from_bytes(file.read(1), byteorder='big')
        rows    = int.from_bytes(file.read(4), byteorder='big')
        cols    = int.from_bytes(file.read(4), byteorder='big')
        frames  = int.from_bytes(file.read(8), byteorder='big')
        temp    = struct.unpack('>f', file.read(4))[0]
        npixels = rows * cols
        stats   = np.asarray( struct.unpack('>' + 'f'*npixels, file.read(4*npixels)) )
        if (reshape):
            stats = stats.reshape(rows, cols)
        return bits, rows, cols, frames, temp, stats

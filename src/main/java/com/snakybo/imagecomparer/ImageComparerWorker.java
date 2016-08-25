// The MIT License(MIT)
//
// Copyright(c) 2016 Kevin Krol
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.snakybo.imagecomparer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Snakybo
 * @since 1.0
 */
public final class ImageComparerWorker
{
	private static Map<Path, BufferedImage> cache = new HashMap<>();
	
	private ImageComparerWorker()
	{
		throw new AssertionError();
	}
	
	/**
	 * <p>Compare a bunch of images.</p>
	 *
	 * <p>This will compare all specified {@code targets} on a pixel-by-pixel basis.
	 * If it determines a specific pair of images has already been compared, it will skip ahead.</p>
	 *
	 * @param targets The images to compare.
	 * @throws IOException
	 */
	public static void compare(Iterable<Path> targets) throws IOException
	{
		for(Path file1 : targets)
		{
			BufferedImage image1 = getBufferedImage(file1);
			
			for(Path file2 : targets)
			{
				if(file1.equals(file2))
				{
					continue;
				}
				
				if(!alreadyCompared(file1, file2))
				{
					System.out.println("Comparing " + file1.getFileName() + " to " + file2.getFileName());
					
					BufferedImage image2 = getBufferedImage(file2);
					
					if(compare(image1, image2))
					{
						registerDuplicate(file1, file2);
					}
				}
			}
		}
		
		cache.clear();
	}
	
	private static void registerDuplicate(Path file1, Path file2)
	{
		for(DuplicateInfo duplicateInfo : DuplicateInfo.infos)
		{
			if(duplicateInfo.getSource().equals(file1))
			{
				duplicateInfo.addDuplicate(file2);
				return;
			}
			else if(duplicateInfo.getSource().equals(file2))
			{
				duplicateInfo.addDuplicate(file1);
				return;
			}
		}
		
		DuplicateInfo duplicateInfo = new DuplicateInfo(file1);
		duplicateInfo.addDuplicate(file2);
	}
	
	private static boolean compare(BufferedImage image1, BufferedImage image2)
	{
		if(image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight())
		{
			return false;
		}
		
		for(int x = 0; x < image1.getWidth(); x++)
		{
			for(int y = 0; y < image1.getHeight(); y++)
			{
				if(image1.getRGB(x, y) != image2.getRGB(x, y))
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	private static boolean alreadyCompared(Path file1, Path file2)
	{
		for(DuplicateInfo duplicateInfo : DuplicateInfo.infos)
		{
			if(duplicateInfo.getDuplicates().contains(file1) || duplicateInfo.getDuplicates().contains(file2))
			{
				return true;
			}
		}
		
		return false;
	}
	
	private static BufferedImage getBufferedImage(Path file) throws IOException
	{
		if(cache.containsKey(file))
		{
			return cache.get(file);
		}
		
		BufferedImage image = ImageIO.read(file.toFile());
		cache.put(file, image);
		
		return image;
	}
}

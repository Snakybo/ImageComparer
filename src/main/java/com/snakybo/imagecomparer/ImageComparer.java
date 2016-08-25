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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Snakybo
 * @since 1.0
 */
public class ImageComparer
{
	/**
	 * <p>Compare all images in the specified {@code directory}, the application will
	 * exit if the {@code directory} does not exists, or is invalid.</p>
	 *
	 * @param directory The directory to compare.
	 * @throws IOException
	 */
	public final void compare(String directory) throws IOException
	{
		Path directoryPath = Paths.get(directory);
		
		// Check if the directory exists, and is a folder (not a file, etc)
		if(!Files.exists(directoryPath) || !Files.isDirectory(directoryPath))
		{
			System.err.println("Specified path (" + directory + ") is not a valid directory.");
			System.exit(1);
		}
		
		DirectoryStream<Path> files = Files.newDirectoryStream(directoryPath);
		List<Path> imageFiles = new ArrayList<>();
		
		for(Path file : files)
		{
			// Check if the file is a regular file (not a folder, etc), and it is a valid image file
			// (has the image mime type)
			if(Files.isRegularFile(file) && isValidImage(file))
			{
				imageFiles.add(file);
			}
		}
		
		compareImpl(imageFiles);
	}
	
	/**
	 * <p>Compare a bunch of images. The application will exit if one or more of the {@code images} does not exist,
	 * or is invalid.</p>
	 *
	 * @param images The images to compare.
	 * @throws IOException
	 */
	public final void compare(String... images) throws IOException
	{
		List<Path> imageFiles = new ArrayList<>();
		
		for(String image : images)
		{
			Path file = Paths.get(image);
			
			// Check if the file exists, and is a regular file (not a folder, etc)
			if(!Files.exists(file) || !Files.isRegularFile(file))
			{
				System.err.println("Unable to read one or more files: " + image + ", it may not exist or not be a valid file");
				System.exit(1);
			}
			
			// Check if the file is a valid image file (has the image mime type)
			if(!isValidImage(file))
			{
				System.err.println(image + " is not a valid image file");
				System.exit(1);
			}
			
			imageFiles.add(file);
		}
		
		compareImpl(imageFiles);
	}
	
	private void compareImpl(List<Path> images)
	{
		System.out.println("Comparing " + images.size() + " images...");
		
		try
		{
			// Tell the worker to compare all images
			ImageComparerWorker.compare(images);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		// Output the results
		System.out.println();
		
		int numDuplicates = 0;
		for(DuplicateInfo duplicateInfo : DuplicateInfo.infos)
		{
			numDuplicates += duplicateInfo.getNumDuplicates();
		}
		
		System.out.println("Complete, " +
				(numDuplicates > 0 ? numDuplicates + " duplicate images found" : "no duplicate images found"));
		
		for(DuplicateInfo duplicateInfo : DuplicateInfo.infos)
		{
			duplicateInfo.sort();
			duplicateInfo.print();
			
			System.out.println();
		}
		
		System.exit(0);
	}
	
	private boolean isValidImage(Path file) throws IOException
	{
		String mimetype = Files.probeContentType(file);
		String type = mimetype.split("/")[0];
		
		return type.equals("image");
	}
	
	public static void main(String[] args) throws IOException
	{
		ImageComparer imageComparer = new ImageComparer();
		
		if(args.length <= 0)
		{
			System.err.println("Usage:");
			System.err.println(" ImageComparer <directory>");
			System.err.println(" ImageComparer <file1> <file2> [file3] ...");
			System.exit(1);
		}
		else if(args.length == 1)
		{
			// args[0] should be a directory
			imageComparer.compare(args[0]);
		}
		else if(args.length > 1)
		{
			// args should be an array of files
			imageComparer.compare(args);
		}
	}
}

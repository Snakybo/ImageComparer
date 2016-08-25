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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Snakybo
 * @since 1.0
 */
public final class DuplicateInfo
{
	public static final List<DuplicateInfo> infos = new ArrayList<>();
	
	private Path source;
	private List<Path> duplicates;
	
	public DuplicateInfo(Path source)
	{
		this.source = source;
		this.duplicates = Collections.synchronizedList(new ArrayList<>());
		
		infos.add(this);
	}
	
	public final void sort()
	{
		Path original = null;
		
		List<Path> all = new ArrayList<>();
		all.add(source);
		all.addAll(duplicates);
		
		try
		{
			BasicFileAttributes originalAttribs = null;
			
			for(Path duplicate : all)
			{
				BasicFileAttributes duplicateAttribs = Files.readAttributes(duplicate, BasicFileAttributes.class);
				
				if(
						original == null ||
						originalAttribs.creationTime().compareTo(duplicateAttribs.creationTime()) > 0 ||
						originalAttribs.lastModifiedTime().compareTo(duplicateAttribs.lastModifiedTime()) > 0)
				{
					original = duplicate;
					originalAttribs = duplicateAttribs;
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		duplicates.add(source);
		source = original;
		duplicates.remove(original);
	}
	
	public final void print()
	{
		System.out.println(source.getFileName() + " (" + duplicates.size() + " duplicates)");
		
		for(int i = 0; i < duplicates.size(); i++)
		{
			System.out.println(" " + (i + 1) + ": " + duplicates.get(i).getFileName());
		}
	}
	
	public final void addDuplicate(Path path)
	{
		if(!duplicates.contains(path))
		{
			duplicates.add(path);
		}
	}
	
	public final Path getSource()
	{
		return source;
	}
	
	public final List<Path> getDuplicates()
	{
		return duplicates;
	}
	
	public final int getNumDuplicates()
	{
		return duplicates.size();
	}
}

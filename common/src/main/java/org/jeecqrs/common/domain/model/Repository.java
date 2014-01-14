/*
 * Copyright (c) 2013-2014 Red Rainbow IT Solutions GmbH, Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.jeecqrs.common.domain.model;

/**
 * Provides the ability to load and store objects.
 * 
 * @param <T>  the type of the objects
 * @param <ID>  the type to identify the objects
 * @param <CID>  the type to identify commits
 */
public interface Repository<T, ID, CID> {

    /**
     * Retrieves the object with the given identity.
     * Must exist.
     * 
     * @param id  the id of the object to retrieve
     * @return    the object with the given id
     */
    T ofIdentity(ID id);

    /**
     * Adds an object to the repository.
     *
     * @param object  the object to add
     * @param commitId  unique id for this commit
     */
    void add(T object, CID commitId);

    /**
     * Saves an instance of the object to the repository.
     * 
     * @param object the object to save
     * @param commitId  unique id for this commit
     */
    void save(T object, CID commitId);
    
}


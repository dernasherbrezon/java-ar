package com.google.code.ar;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


class ArEntryValidator {

    static void validate(ArEntry entry) {
        if( entry == null ) {
            return;
        }
        if( entry.getFilename() == null || entry.getFilename().trim().length() == 0 ) {
            throw new IllegalArgumentException("entry filename should be specified");
        }
        if( entry.getFileMode() < 0 ) {
            throw new IllegalArgumentException("entry file mode should be positive. got: " + entry.getFileMode());
        }
        if( entry.getGroupId() < 0 ) {
            throw new IllegalArgumentException("entry group id should be positive. got: " + entry.getGroupId());
        }
        if( entry.getOwnerId() < 0 ) {
            throw new IllegalArgumentException("entry owner id should be positive. got: " + entry.getOwnerId());
        }
    }
    
    static void validateInMemoryEntry(ArEntry entry) {
    	validate(entry);
        if( entry.getData() == null ) {
            throw new IllegalArgumentException("entry data should be specified or have 0 lenght");
        }
    }
    
}

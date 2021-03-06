/*
 *  Copyright 2017 Patrick Favre-Bulle
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package at.favre.tools.dice.encode.languages;

import at.favre.lib.bytes.Bytes;

/**
 * Encodes in swift syntax byte array (e.g. <code>var foo:[UInt8] = [0x00, 0x11, 0x22 ]</code>)
 */
public class SwiftEncoder extends AProgrammingLanguagesEncoder {
    @Override
    public String encode(byte[] array) {
        return encodeInternal(array, "[UInt8] = [", "]", ",", b -> "0x" + Bytes.from(b).encodeHex(true));
    }

    @Override
    public String[] names() {
        return new String[]{"swift"};
    }

    @Override
    String getProgrammingLanguageName() {
        return "Swift";
    }
}

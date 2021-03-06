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

package at.favre.tools.dice.service.hotbits;

import at.favre.tools.dice.service.AServiceHandler;
import com.github.fzakaria.ascii85.Ascii85;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * HotBits: Genuine random numbers, generated by radioactive decay
 * <p>
 * See https://www.fourmilab.ch/hotbits/
 */
public final class HotbitsServiceHandler extends AServiceHandler {
    final static int ENTROPY_SEED_LENGTH_BYTE = 24;

    public HotbitsServiceHandler(boolean debug) {
        super(debug);
    }

    @Override
    public Result<Void> getRandom() {
        long startTime = System.currentTimeMillis();

        OkHttpClient client = createClient();
        Exception error = null;
        String errMsg = null;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.fourmilab.ch/")
                .client(client)
                .build();

        HotbitsService service = retrofit.create(HotbitsService.class);

        try {
            Response<ResponseBody> response = service.getRandom(createHeaderMap(), ENTROPY_SEED_LENGTH_BYTE, new String(Ascii85.decode(API_KEY), StandardCharsets.UTF_8)).execute();
            if (response != null && response.isSuccessful() && response.body() != null) {
                byte[] rawResponse = response.body().bytes();
                return new Result<>(getName(), rawResponse, null, System.currentTimeMillis() - startTime);
            }
        } catch (UnknownHostException e) {
            error = e;
            errMsg = "Cannot resolve host. Is the device offline?";
        } catch (Exception e) {
            error = e;
            errMsg = "Error during http request: " + e.getMessage();
        }

        return new Result<>(getName(), error, errMsg);
    }

    @Override
    public String getName() {
        return "Hotbits";
    }

    private static final String API_KEY = (new Object() {
        int t;

        public String toString() {
            byte[] buf = new byte[34];
            t = -2039461835;
            buf[0] = (byte) (t >>> 17);
            t = -929408877;
            buf[1] = (byte) (t >>> 15);
            t = 575293644;
            buf[2] = (byte) (t >>> 6);
            t = -325769287;
            buf[3] = (byte) (t >>> 18);
            t = 421045706;
            buf[4] = (byte) (t >>> 5);
            t = 1178835950;
            buf[5] = (byte) (t >>> 7);
            t = 935373502;
            buf[6] = (byte) (t >>> 24);
            t = 1573330001;
            buf[7] = (byte) (t >>> 18);
            t = -1150877348;
            buf[8] = (byte) (t >>> 19);
            t = -257198827;
            buf[9] = (byte) (t >>> 2);
            t = 473371099;
            buf[10] = (byte) (t >>> 23);
            t = 491600322;
            buf[11] = (byte) (t >>> 3);
            t = 149615520;
            buf[12] = (byte) (t >>> 18);
            t = -1024115474;
            buf[13] = (byte) (t >>> 10);
            t = 306818716;
            buf[14] = (byte) (t >>> 13);
            t = -2073190904;
            buf[15] = (byte) (t >>> 20);
            t = 1754566129;
            buf[16] = (byte) (t >>> 18);
            t = 591077053;
            buf[17] = (byte) (t >>> 19);
            t = 864352475;
            buf[18] = (byte) (t >>> 23);
            t = -839221067;
            buf[19] = (byte) (t >>> 22);
            t = -1009617859;
            buf[20] = (byte) (t >>> 4);
            t = 969897130;
            buf[21] = (byte) (t >>> 4);
            t = -1740444710;
            buf[22] = (byte) (t >>> 12);
            t = -1964194818;
            buf[23] = (byte) (t >>> 13);
            t = 1376514350;
            buf[24] = (byte) (t >>> 3);
            t = 1220642786;
            buf[25] = (byte) (t >>> 21);
            t = 1315800381;
            buf[26] = (byte) (t >>> 5);
            t = -972926411;
            buf[27] = (byte) (t >>> 6);
            t = -1222995758;
            buf[28] = (byte) (t >>> 15);
            t = -1740496774;
            buf[29] = (byte) (t >>> 17);
            t = -752960245;
            buf[30] = (byte) (t >>> 20);
            t = 1319005613;
            buf[31] = (byte) (t >>> 24);
            t = 35721772;
            buf[32] = (byte) (t >>> 6);
            t = 1032122760;
            buf[33] = (byte) (t >>> 3);
            return new String(buf);
        }
    }.toString());

}

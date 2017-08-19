package at.favre.tools.dice.service;

import at.favre.tools.dice.service.model.RandomOrgBlobRequest;
import at.favre.tools.dice.service.model.RandomOrgBlobResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RandomOrgService {
    @POST("/json-rpc/1/invoke")
    Call<RandomOrgBlobResponse> getRandom(@Body RandomOrgBlobRequest request);
}
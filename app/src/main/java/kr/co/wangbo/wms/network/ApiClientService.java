package kr.co.wangbo.wms.network;

import java.util.concurrent.TimeUnit;


import kr.co.wangbo.wms.BuildConfig;
import kr.co.wangbo.wms.model.ResultModel;
import kr.co.wangbo.wms.model.ShipDetailModel;
import kr.co.wangbo.wms.model.ShipModel;
import kr.co.wangbo.wms.model.ShipPopModel;
import kr.co.wangbo.wms.model.ShipReqModel;
import kr.co.wangbo.wms.model.UserInfoModel;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiClientService {
    /**
     * 로그인
     * @param proc  프로시져
     * @param user_id 아이디
     * @param pass 비밀번호
     * @return
     */
    @POST("R2JsonProc.asp")
    Call<UserInfoModel> postLogin(
            @Query("proc") String proc,
            @Query("param1") String user_id,
            @Query("param2") String pass
    );

    /**
     * 로그인
     * @param proc  프로시져
     * @param mac   맥주소
     * @param date 출하일자
     * @param user_id 로그인아이디
     * @param ship_no 출하지시번호
     * @return
     */
    @POST("R2JsonProc.asp")
    Call<ShipModel> requestShipSave(
            @Query("proc") String proc,
            @Query("param1") String mac,
            @Query("param2") String date,
            @Query("param3") String user_id,
            @Query("param4") String ship_no
    );

    /**
     * 제품출하 리스트
     * @param proc  프로시져
     * @param mac 맥주소
     * @param barcode 출하지시번호
     */
    @POST("R2JsonProc.asp")
    Call<ShipModel> pda_req_itmlist(
            @Query("proc") String proc,
            @Query("param1") String mac,
            @Query("param2") String barcode
    );

    /**
     * 현퓸표 바코드 스캔
     * @param proc  프로시져
     * @param mac 맥주소
     * @param req_no 출하지시번호
     * @param barcode 바코드
     */
    @POST("R2JsonProc.asp")
    Call<ShipModel> pda_req_Scan(
            @Query("proc") String proc,
            @Query("param1") String mac,
            @Query("param2") String req_no,
            @Query("param3") String barcode
    );

    /**
     * 현품표 리스트
     * @param proc  프로시져
     * @param mac mac주소
     * @param barcode 출하지시번호
     */
    @POST("R2JsonProc.asp")
    Call<ShipModel> pda_req_itmDetailList(
            @Query("proc") String proc,
            @Query("param1") String mac,
            @Query("param2") String barcode
    );

    /**
     * 팝업 현품표 리스트
     * @param proc  프로시져
     * @param mac mac주소
     * @param barcode 출하지시번호
     * @param itm_code 아이템코드
     */
    @POST("R2JsonProc.asp")
    Call<ShipPopModel> sp_pda_barlist(
            @Query("proc") String proc,
            @Query("param1") String mac,
            @Query("param2") String barcode,
            @Query("param3") String itm_code
    );

    /**
     * 현품표 삭제
     * @param proc  프로시져
     * @param mac mac주소
     * @param req_code 출하지시번호
     * @param barcode 현품표번호
     */
    @POST("R2JsonProc.asp")
    Call<ShipPopModel> sp_pda_pack_del(
            @Query("proc") String proc,
            @Query("param1") String mac,
            @Query("param2") String req_code,
            @Query("param3") String barcode
    );

    /**
     * 출하지시서 조회 팝업
     * @param proc  프로시져
     * @param date 일자
     */
    @POST("R2JsonProc.asp")
    Call<ShipReqModel> sp_pda_reqlist(
            @Query("proc") String proc,
            @Query("param1") String date
    );

    /**
     * 출하등록저장
     * */
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    @POST("R2JsonProc_ship_add.asp")
    Call<ShipModel> postShipSave(
            @Body RequestBody body
    );

    //R2JsonProc_plt_mrg_save.asp

    //로그 찍기
    //태그 OkHttp 입력(adb logcat OkHttp:D *:S)
    // HttpLoggingInterceptor.Level.BODY  모든 바디 로그 온
    // HttpLoggingInterceptor.Level.NONE  로그 오프
    public static final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

    //타임아웃 1분
    public static final OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .addInterceptor(interceptor);

    //Gson으로 리턴
    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BuildConfig.API_SERVER)
            .addConverterFactory(GsonConverterFactory.create())
            .client(builder.build())
            .build();

    //String으로 리턴
    public static final Retrofit retrofitString = new Retrofit.Builder()
            .baseUrl(BuildConfig.API_SERVER)
            .addConverterFactory(new ToStringConverterFactory())
            .client(builder.build())
            .build();
}

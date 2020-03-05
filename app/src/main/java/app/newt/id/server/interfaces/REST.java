package app.newt.id.server.interfaces;

import java.util.Map;

import app.newt.id.server.response.BaseResponse;
import app.newt.id.server.response.ChallengeResponse;
import app.newt.id.server.response.ChatResponse;
import app.newt.id.server.response.DialogsResponse;
import app.newt.id.server.response.FeatureResponse;
import app.newt.id.server.response.LessonsResponse;
import app.newt.id.server.response.PrivateTeacherResponse;
import app.newt.id.server.response.RatingResponse;
import app.newt.id.server.response.RegistrationResponse;
import app.newt.id.server.response.TeachersResponse;
import app.newt.id.server.response.TransactionResponse;
import app.newt.id.server.response.UpdateResponse;
import app.newt.id.server.response.UserResponse;
import io.reactivex.Single;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface REST {
    @GET("test")
    Single<Response<BaseResponse>> test();

    @GET("update")
    Single<Response<UpdateResponse>> updateApp();

    @GET("features")
    Single<Response<FeatureResponse>> loadFeatures();

    @POST("register/phone")
    Single<Response<RegistrationResponse>> registerPhone(@Body Map<String, Object> data);

    @POST("register/resend_code")
    Single<Response<RegistrationResponse>> resendCode(@Body Map<String, Object> data);

    @POST("register/verify_code")
    Single<Response<RegistrationResponse>> verifyCode(@Body Map<String, Object> data);

    @POST("register/student")
    Single<Response<UserResponse>> registerStudent(@Body Map<String, Object> data);

    @POST("reset_password/phone")
    Single<Response<RegistrationResponse>> validatePhone(@Body Map<String, Object> data);

    @POST("reset_password/password")
    Single<Response<RegistrationResponse>> resetPassword(@Body Map<String, Object> data);

    @POST("login")
    Single<Response<UserResponse>> login(@Body Map<String, Object> data);

    @POST("re_register/phone")
    Single<Response<RegistrationResponse>> reregisterPhone(@Body Map<String, Object> data);

    @POST("re_register/reset_phone")
    Single<Response<UserResponse>> resetPhone(@Body Map<String, Object> data);

    @POST("profile/apply_promo_code")
    Single<Response<UserResponse>> applyPromoCode(@Query("promo_code") String promoCode);

    @Multipart
    @POST("update_profile")
    Single<Response<UserResponse>> updateProfile(@PartMap Map<String, RequestBody> data);

    @GET("base/load_dialogs")
    Single<Response<DialogsResponse>> loadDialogs();

    @GET("base/load_teachers")
    Single<Response<TeachersResponse>> loadTeachers();

    @GET("base/fetch_teachers")
    Single<Response<TeachersResponse>> fetchTeachers();

    @GET("base/fetch_profiles")
    Single<Response<UserResponse>> fetchProfiles(@Query("codes") String codes);

    @GET("get_rating")
    Single<Response<RatingResponse>> getRating(@Query("teacher_id") int id, @Query("lesson_id") int lessonId);

    @POST("set_rating")
    Single<Response<RatingResponse>> setRating(@Body Map<String, Object> data);

    @GET("get_avg_rating")
    Single<Response<RatingResponse>> getAvgRating(@Query("teacher_id") int teacherId);

    @Multipart
    @POST("chat/add")
    Single<Response<ChatResponse>> addChat(@PartMap Map<String, RequestBody> data);

    @GET("chat/load_queue")
    Single<Response<DialogsResponse>> loadQueue();

    @POST("chat/mark_queue")
    Single<Response<BaseResponse>> markQueue();

    @GET("get_transaction_datas")
    Single<Response<TransactionResponse>> getTransactionDatas();

    @GET("base/load_private_teachers")
    Single<Response<PrivateTeacherResponse>> loadPrivateTeachers(@Query("private_lesson_id") int gradeId);

    @POST("challenge/register")
    Single<Response<BaseResponse>> registerChallenger();

    @GET("challenge/load_questions")
    Single<Response<ChallengeResponse>> loadQuestions(@Query("lesson_id") int lessonId);

    @GET("challenge/load_question_detail")
    Single<Response<ChallengeResponse>> loadQuestionDetail(@Query("id") int id);

    @POST("challenge/submit_answer")
    Single<Response<ChallengeResponse>> submitAnswer(@Query("question_id") int id, @Query("answer") int answer);

    @GET("challenge/load_ranks")
    Single<Response<ChallengeResponse>> loadRanks(@Query("lesson_id") int lessonId);

    @GET("challenge/load_records")
    Single<Response<ChallengeResponse>> loadRecords(@Query("id") int challengerId, @Query("lesson_id") int lessonId);

    @GET("challenge/load_histories")
    Single<Response<ChallengeResponse>> loadHistories(@Query("lesson_id") int lessonId, @Query("last_id") int lastId);

    @Streaming
    @GET
    Single<Response<ResponseBody>> downloadDocument(@Url String url);
}
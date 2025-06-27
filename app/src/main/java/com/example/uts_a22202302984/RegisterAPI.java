package com.example.uts_a22202302984;

import com.example.uts_a22202302984.model.OrderHistory;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RegisterAPI {
    @FormUrlEncoded
    @POST("get_login.php")
    Call<ResponseBody> login(
            @Field("email") String email,
            @Field("password") String password
    );

    @GET("get_profile.php")
    Call<ResponseBody> getProfile(@Query("email") String email);


    @FormUrlEncoded
    @POST("post_register.php")
    Call<ResponseBody> register(@Field("email") String email,
                                @Field("nama") String nama,
                                @Field("password") String password);

    @Multipart
    @POST("post_profile.php")
    Call<ResponseBody> updateProfile(
            @Part("nama") RequestBody nama,
            @Part("alamat") RequestBody alamat,
            @Part("kota") RequestBody kota,
            @Part("provinsi") RequestBody provinsi,
            @Part("telp") RequestBody telp,
            @Part("kodepos") RequestBody kodepos,
            @Part("email") RequestBody email,
            @Part MultipartBody.Part foto
    );

    @Multipart
    @POST("upload_profile.php")
    Call<ResponseBody> uploadFoto(
            @Part("email") RequestBody email,
            @Part MultipartBody.Part image
    );

    @GET("get_produk.php")
    Call<List<Product>> getProducts();

    @FormUrlEncoded
    @POST("update_viewer.php")
    Call<ResponseBody> updateViewer(@Field("id_produk") String idProduk);

    @FormUrlEncoded
    @POST("post_order.php")
    Call<ResponseBody> postOrder(
            @Field("email") String email,
            @Field("nama") String nama,
            @Field("alamat") String alamat,
            @Field("telp") String telp,
            @Field("kodepos") String kodepos,
            @Field("provinsi") String provinsi,
            @Field("kota") String kota,
            @Field("metode_pembayaran") String metode,
            @Field("subtotal") int subtotal,
            @Field("ongkir") int ongkir,
            @Field("estimasi") String estimasi,
            @Field("total") int total,
            @Field("status") String status
    );

    @FormUrlEncoded
    @POST("post_order_detail.php")
    Call<ResponseBody> postOrderDetail(
            @Field("order_id") int orderId,
            @Field("id_produk") String produkId,
            @Field("nama_produk") String namaProduk,
            @Field("harga") int harga,
            @Field("jumlah") int jumlah,
            @Field("subtotal") int subtotal
    );

    @FormUrlEncoded
    @POST("get_order_history.php")
    Call<List<OrderHistory>> getOrderHistory(@Field("email") String email);

    @POST("post_change_password.php")
    Call<ResponseBody> changePassword(
            @Query("email") String email,
            @Query("old_password") String oldPassword,
            @Query("new_password") String newPassword
    );

    @Multipart
    @POST("upload_bukti.php")
    Call<ResponseBody> uploadBuktiTransfer(
            @Part("order_id") RequestBody orderId,
            @Part MultipartBody.Part bukti
    );


    @GET("get_provinsi.php")
    Call<ResponseBody> getProvinsi();

    @GET("get_kota.php")
    Call<ResponseBody> getKota(
            @Query("id_provinsi") String idProvinsi
    );
}

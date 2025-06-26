// OrderHistoryAdapter.java
package com.example.uts_a22202302984;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uts_a22202302984.model.OrderHistory;

import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<OrderHistory> orderList;
    private final OnUploadClickListener uploadClickListener;

    public interface OnUploadClickListener {
        void onUploadClicked(OrderHistory order);
    }

    public OrderHistoryAdapter(Context context, List<OrderHistory> orderList, OnUploadClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.uploadClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderHistory order = orderList.get(position);

        holder.tvNomorOrder.setText("Order ID:" + order.getOrderId() + " - " + order.getTanggal());
        holder.tvAlamatOrder.setText("Alamat: " + order.getAlamat());
        holder.tvKotaProvinsi.setText("Kota: " + order.getKota() + ", Provinsi: " + order.getProvinsi());
        holder.tvSubtotalOngkir.setText("Subtotal: Rp" + order.getSubtotal() + " | Ongkir: Rp" + order.getOngkir());
        holder.tvTotalBayar.setText("Total Bayar: Rp" + order.getTotal());
        holder.tvMetodeEstimasi.setText("Metode: " + order.getMetode() + " | Estimasi: " + order.getEstimasi());
        holder.tvStatus.setText("Status: " + order.getStatus());


        // Tombol Upload Bukti
        holder.btnUploadBukti.setOnClickListener(v -> {
            if (uploadClickListener != null) {
                uploadClickListener.onUploadClicked(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomorOrder, tvAlamatOrder, tvKotaProvinsi, tvSubtotalOngkir, tvTotalBayar, tvMetodeEstimasi, tvStatus;
        Button btnUploadBukti;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomorOrder = itemView.findViewById(R.id.tvNomorOrder);
            tvAlamatOrder = itemView.findViewById(R.id.tvAlamatOrder);
            tvKotaProvinsi = itemView.findViewById(R.id.tvKotaProvinsi);
            tvSubtotalOngkir = itemView.findViewById(R.id.tvSubtotalOngkir);
            tvTotalBayar = itemView.findViewById(R.id.tvTotalBayar);
            tvMetodeEstimasi = itemView.findViewById(R.id.tvMetodeEstimasi);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnUploadBukti = itemView.findViewById(R.id.btnUploadBukti);
        }
    }
}
package com.lleans.spp_kelompok_2.ui.main.petugas.aktivitas;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.lleans.spp_kelompok_2.R;
import com.lleans.spp_kelompok_2.domain.Utils;
import com.lleans.spp_kelompok_2.domain.model.pembayaran.PembayaranData;
import com.lleans.spp_kelompok_2.domain.model.pembayaran.PembayaranSharedModel;
import com.lleans.spp_kelompok_2.ui.launcher.LauncherFragment;
import com.lleans.spp_kelompok_2.ui.utils.UtilsUI;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class AktivitasCardAdapter extends RecyclerView.Adapter<AktivitasCardAdapter.AktivitasCardViewHolder> implements Filterable {

    private final NavController controller;
    private Context context;

    private final List<PembayaranData> listData, listAll;
    private final String fromWhere;
    private int orange, count;
    private String date;

    public AktivitasCardAdapter(List<PembayaranData> list, NavController navController, String fromWhere) {
        this.listData = list;
        this.listAll = new ArrayList<>(list);
        this.controller = navController;
        this.fromWhere = fromWhere;
    }

    @NonNull
    @Override
    public AktivitasCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_aktivitas, parent, false);

        orange = view.getResources().getColor(R.color.orange);
        context = view.getContext();
        return new AktivitasCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AktivitasCardViewHolder holder, int position) {
        PembayaranData data = listData.get(position);

        String parsed = Utils.parseLongtoStringDate(Utils.parseServerStringtoLongDate(data.getUpdatedAt(), "yyyy-MM-dd"), "dd MMMM yyyy");
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.add(Calendar.DAY_OF_YEAR, -1);
        c2.setTimeInMillis(Utils.parseServerStringtoLongDate(data.getUpdatedAt(), "yyyy-MM-dd"));
        if (!parsed.equals(date) && !fromWhere.equals("homepage") && !fromWhere.equals("detailPetugas")) {
            this.date = parsed;
            if (parsed.equals(Utils.getCurrentDateAndTime("dd MMMM yyyy"))) {
                holder.sectionText.setText("Hari ini");
            } else if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)) {
                holder.sectionText.setText("Kemarin");
            } else {
                holder.sectionText.setText(parsed);
            }
            holder.section.setVisibility(View.VISIBLE);
        }
        holder.name.setText(data.getSiswa().getNama());
        holder.kelas.setText(data.getSiswa().getKelas().getNamaKelas());
        if (data.getSpp() != null && Utils.statusPembayaran(data.getSpp().getNominal(), data.getJumlahBayar())) {
            holder.status.setText("Belum Lunas");
            holder.status.setTextColor(orange);
            holder.nominalkurang.setText(Utils.kurangBayar(data.getSpp().getNominal(), data.getJumlahBayar()));
        } else {
            holder.status.setText("Lunas");
            holder.nominalkurang.setText(Utils.formatRupiah(data.getJumlahBayar()));
        }
        holder.cardView.setOnClickListener(v -> {
            PembayaranSharedModel sharedModel = new ViewModelProvider((LauncherFragment) context).get(PembayaranSharedModel.class);
            sharedModel.updateData(data);
            if (fromWhere.equals("homepage")) {
                controller.navigate(R.id.action_homepage_petugas_to_rincianTransaksi_siswa);
            } else if (fromWhere.equals("detailPetugas")) {
                controller.navigate(R.id.action_detailPetugas_petuga_to_rincianTransaksi_siswa);
            } else {
                controller.navigate(R.id.action_aktivitas_petugas_to_rincianTransaksi_siswa);
            }
        });
        UtilsUI.simpleAnimation(holder.itemView);
    }

    @Override
    public int getItemCount() {
        if (count >= listData.size() || count == 0) {
            return listData.size();
        } else {
            return count;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull AktivitasCardViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        holder.clearAnimation();
    }

    public int setItemCount(int count) {
        return this.count = count;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String year = Utils.parseLongtoStringDate(Long.valueOf(constraint.toString()), "yyyy");
                String month = Utils.parseLongtoStringDate(Long.valueOf(constraint.toString()), "MM");
                List<PembayaranData> filteredlist = new ArrayList<>();

                for (PembayaranData data : listAll) {
                    long date = Utils.parseServerStringtoLongDate(data.getUpdatedAt(), "yyyy-MM");
                    if (Utils.parseLongtoStringDate(date, "yyyy").equals(year) && Utils.parseLongtoStringDate(date, "MM").equals(month) ) {
                        filteredlist.add(data);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredlist;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                listData.clear();
                listData.addAll((Collection<? extends PembayaranData>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    public static class AktivitasCardViewHolder extends RecyclerView.ViewHolder {
        TextView name, kelas, status, nominalkurang, sectionText;
        CardView cardView;
        RelativeLayout section;

        public AktivitasCardViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.title);
            kelas = itemView.findViewById(R.id.kelas);
            status = itemView.findViewById(R.id.status);
            nominalkurang = itemView.findViewById(R.id.nominal);

            cardView = itemView.findViewById(R.id.card);
            sectionText = itemView.findViewById(R.id.sectionText);
            section = itemView.findViewById(R.id.section);
        }

        public void clearAnimation(){
            itemView.clearAnimation();
        }
    }

}

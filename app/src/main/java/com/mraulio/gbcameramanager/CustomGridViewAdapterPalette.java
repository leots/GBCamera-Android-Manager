package com.mraulio.gbcameramanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.mraulio.gbcameramanager.model.GbcPalette;

import java.util.ArrayList;

/**
 * Other way to show images on the GridView, with the Text
 */
public class CustomGridViewAdapterPalette extends ArrayAdapter<GbcPalette> {
    Context context;
    int layoutResourceId;
    ArrayList<GbcPalette> data = new ArrayList<GbcPalette>();
    int lastSelectedPosition = -1; // Inicialmente no hay ningún elemento seleccionado

    public CustomGridViewAdapterPalette(Context context, int layoutResourceId,
                                        ArrayList<GbcPalette> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecordHolder holder = null;
        int selectedColor =Color.parseColor("#ffc36d");

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new RecordHolder();
            holder.txtTitle = (TextView) row.findViewById(R.id.tvPaletteName);
            holder.imageItem = (ImageView) row.findViewById(R.id.imageView);
            holder.cardView = (CardView) row.findViewById(R.id.cardViewPalette);
            row.setTag(holder);
        } else {
            holder = (RecordHolder) row.getTag();
        }
        holder.imageItem.setBackgroundColor(0);

        if (position == lastSelectedPosition) {
            holder.imageItem.setBackgroundColor(selectedColor);
            System.out.println("////////////////////////Cambiando fondo***************");
        }

        Bitmap image = data.get(position).paletteViewer();
        String name = data.get(position).getName();
        holder.txtTitle.setText(name);
        holder.imageItem.setImageBitmap(Bitmap.createScaledBitmap(image, image.getWidth() * 6, image.getHeight() * 6, false));
        return row;
    }

    private class RecordHolder {
        TextView txtTitle;
        CardView cardView;
        ImageView imageItem;

    }
    // Método para actualizar la última posición seleccionada
    public void setLastSelectedPosition(int position) {
        lastSelectedPosition = position;
    }
}
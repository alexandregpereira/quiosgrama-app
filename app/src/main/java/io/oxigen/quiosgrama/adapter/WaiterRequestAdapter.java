package io.oxigen.quiosgrama.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import io.oxigen.quiosgrama.ProductRequest;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.fragment.WaiterRequestFragment;
import io.oxigen.quiosgrama.util.AndroidUtil;
import io.oxigen.quiosgrama.util.ImageUtil;

/**
 * Created by Alexandre on 14/04/2016.
 *
 */
public class WaiterRequestAdapter extends RecyclerView.Adapter<WaiterRequestAdapter.WaiterRequestViewHolder>{
    private Context mContext;
    private WaiterRequestFragment mFragment;
    private ArrayList<ProductRequest> mProdReqList;

    public interface OnItemLongClickListener {
        public ProductRequest onItemLongClicked(ProductRequest productRequest);
    }
    
    public static class WaiterRequestViewHolder extends RecyclerView.ViewHolder {
        protected View v;
        protected ImageView imgProductType;
        protected TextView txtTable;
        protected TextView txtQuantity;
        protected TextView txtProduct;
        protected TextView txtX;
        protected ImageView imgStatus;
        protected TextView txtTime;
        protected TextView txtCheckComplement;
        protected TextView txtTransferRoute;

        public WaiterRequestViewHolder(View v) {
            super(v);
            this.v = v;
            imgProductType = (ImageView) v.findViewById(R.id.imgProductType);
            txtTable = (TextView) v.findViewById(R.id.txtTable);
            txtQuantity = (TextView) v.findViewById(R.id.txtQuantity);
            txtProduct = (TextView) v.findViewById(R.id.txtProduct);
            txtX = (TextView) v.findViewById(R.id.txtX);
            imgStatus = (ImageView) v.findViewById(R.id.imgStatus);
            txtTime = (TextView) v.findViewById(R.id.txtTime);
            txtCheckComplement = (TextView) v.findViewById(R.id.txtCheckComplement);
            txtTransferRoute = (TextView) v.findViewById(R.id.txtTransferRoute);
        }
    }

    public WaiterRequestAdapter(Context context, WaiterRequestFragment fragment, ArrayList<ProductRequest> prodReqList) {
        mContext = context;
        mProdReqList = prodReqList;
        mFragment = fragment;
    }

    @Override
    public WaiterRequestViewHolder onCreateViewHolder(ViewGroup parent,
                                                  int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_request_detail, parent, false);

        return new WaiterRequestViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final WaiterRequestViewHolder holder, final int i) {
        ProductRequest productRequest = mProdReqList.get(i);
        holder.txtTable.setText(productRequest.request.bill.toString());
        holder.txtQuantity.setText(String.valueOf(productRequest.product.quantity));
        holder.txtProduct.setText(productRequest.product.name);

        holder.txtTime.setText(AndroidUtil.calculateDateDays(mContext, productRequest.request.requestTime, new Date()));

        GradientDrawable shapeDrawable = (GradientDrawable) holder.imgProductType.getBackground();
        shapeDrawable.setColor(Color.parseColor(productRequest.product.type.colorId));
        if(holder.imgProductType != null) {
            holder.imgProductType.setImageResource(productRequest.product.type.imageInfoId);
        }

        if (productRequest.complement == null
                || productRequest.complement.description == null
                || productRequest.complement.description.trim().isEmpty()) {
            holder.txtCheckComplement.setVisibility(View.GONE);
        } else {
            holder.txtCheckComplement.setVisibility(View.VISIBLE);
            holder.txtCheckComplement.setText(productRequest.complement.description);
        }

        if (productRequest.transferRoute == null) {
            holder.txtTransferRoute.setVisibility(View.GONE);
        } else {
            holder.txtTransferRoute.setVisibility(View.VISIBLE);
            holder.txtCheckComplement.setVisibility(View.VISIBLE);
            holder.txtTransferRoute.setText(productRequest.transferRoute);
        }

        int color;
        if (!productRequest.valid) {
            color = mContext.getResources().getColor(R.color.text_disable);
            holder.txtQuantity.setTextColor(color);
            holder.txtCheckComplement.setTextColor(color);
            holder.txtTable.setTextColor(color);

            holder.txtProduct.setPaintFlags(holder.txtProduct.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.txtQuantity.setPaintFlags(holder.txtQuantity.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.txtCheckComplement.setPaintFlags(holder.txtCheckComplement.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.txtTable.setPaintFlags(holder.txtTable.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.txtTime.setPaintFlags(holder.txtTime.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            color = mContext.getResources().getColor(android.R.color.black);
            holder.txtQuantity.setTextColor(mContext.getResources().getColor(R.color.red));
            holder.txtCheckComplement.setTextColor(mContext.getResources().getColor(R.color.text_less_focus));
            holder.txtTable.setTextColor(mContext.getResources().getColor(R.color.text_less_focus));

            holder.txtProduct.setPaintFlags(holder.txtProduct.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.txtQuantity.setPaintFlags(holder.txtQuantity.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.txtCheckComplement.setPaintFlags(holder.txtCheckComplement.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.txtTable.setPaintFlags(holder.txtTable.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.txtTime.setPaintFlags(holder.txtTime.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.txtProduct.setTextColor(color);
        holder.txtX.setTextColor(color);
        holder.txtTime.setTextColor(color);

        setImageStatus(holder, productRequest);

        holder.v.setTag(productRequest);
        holder.v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ProductRequest prodReq = mFragment.onItemLongClicked((ProductRequest) v.getTag());
                if(prodReq != null) {
                    setImageStatus(holder, prodReq);
                    return true;
                }

                return false;
            }
        });
    }

    private void setImageStatus(WaiterRequestViewHolder holder, ProductRequest productRequest){
        if(productRequest.request.syncStatus == KeysContract.NO_SYNCHRONIZED_STATUS_KEY){
            holder.imgStatus.setImageBitmap(ImageUtil.changeColorIconBitmap(mContext, R.drawable.ic_check, R.color.red));
        }
        else if(productRequest.status == ProductRequest.NOT_VISUALIZED_STATUS){
            if(productRequest.syncStatus == KeysContract.NO_SYNCHRONIZED_STATUS_KEY){
                holder.imgStatus.setImageBitmap(ImageUtil.changeColorIconBitmap(mContext, R.drawable.ic_check, R.color.red));
            }
            else{
                holder.imgStatus.setImageBitmap(ImageUtil.changeColorIconBitmap(mContext, R.drawable.ic_check, R.color.green));
            }
        }
        else if(productRequest.status == ProductRequest.VISUALIZED_STATUS){
            if(productRequest.syncStatus == KeysContract.NO_SYNCHRONIZED_STATUS_KEY){
                holder.imgStatus.setImageBitmap(ImageUtil.changeColorIconBitmap(mContext, R.drawable.ic_check_all, R.color.red));
            }
            else{
                holder.imgStatus.setImageBitmap(ImageUtil.changeColorIconBitmap(mContext, R.drawable.ic_check_all, R.color.green));
            }
        }
        else if(productRequest.status == ProductRequest.READY_STATUS){
            if(productRequest.syncStatus == KeysContract.NO_SYNCHRONIZED_STATUS_KEY){
                holder.imgStatus.setImageBitmap(ImageUtil.changeColorIconBitmap(mContext, R.drawable.ic_delivery, R.color.red));
            }
            else{
                holder.imgStatus.setImageBitmap(ImageUtil.changeColorIconBitmap(mContext, R.drawable.ic_delivery, R.color.green));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mProdReqList.size();
    }
}

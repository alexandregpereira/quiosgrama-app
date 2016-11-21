package io.oxigen.quiosgrama.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.oxigen.quiosgrama.Functionary;
import io.oxigen.quiosgrama.Product;
import io.oxigen.quiosgrama.QuiosgramaApp;
import io.oxigen.quiosgrama.R;
import io.oxigen.quiosgrama.data.KeysContract;
import io.oxigen.quiosgrama.fragment.SendRequestFragment;

public class AutoCompleteProductAdapter extends ArrayAdapter<Product> implements Filterable, OnClickListener {

	private final QuiosgramaApp app;
	private HashSet<Product> productList;
	private List<Product> resultados;
	private Filter meuFiltro;
	private int layoutItem;
	private Context context;
	private String receiver;

	public AutoCompleteProductAdapter(Context ctx, List<Product> productList, String receiver) { 

		super(ctx, R.layout.item_product, productList);
		context = ctx;
		this.receiver = receiver;
		this.productList = new HashSet<>(productList);
		this.resultados = new ArrayList<>(productList);
		layoutItem = R.layout.item_product;
		this.meuFiltro = new MeuFiltro();
		app = (QuiosgramaApp) ctx.getApplicationContext();
	}

	@Override
	public int getCount() {
		return resultados.size();
	}

	@Override
	public Product getItem(int position) {

		if (resultados != null && resultados.size() > 0 && position < resultados.size()){        
			return resultados.get(position);      
		} else {        
			return null;     
		}   

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View productItem = convertView;

		if(productItem == null){
			LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
			productItem = inflater.inflate(layoutItem, parent, false);
		}

		Functionary functionary = app.getFunctionarySelected();

		Product product = getItem(position);

		((TextView) productItem.findViewById(R.id.txtProduct)).setText(product.name);
		((TextView) productItem.findViewById(R.id.txtPrice)).setText(SendRequestFragment.convertToPrice(product.price));
		TextView txtQuantity = (TextView) productItem.findViewById(R.id.txtQuantity);
		TextView txtX = (TextView) productItem.findViewById(R.id.txtX);
		TextView txtDescription = (TextView) productItem.findViewById(R.id.txtDescription);

		ImageButton btnAddProduct = (ImageButton) productItem.findViewById(R.id.btnAddProduct);
		ImageButton btnRemoveProduct = (ImageButton) productItem.findViewById(R.id.btnRemoveProduct);
		if(functionary != null) {
			txtQuantity.setVisibility(View.VISIBLE);
			txtX.setVisibility(View.VISIBLE);
			txtDescription.setVisibility(View.GONE);
			btnAddProduct.setVisibility(View.VISIBLE);
			btnRemoveProduct.setVisibility(View.VISIBLE);

			txtQuantity.setText(String.valueOf(product.quantity));
			btnAddProduct.setTag(product);
			btnAddProduct.setOnClickListener(this);

			btnRemoveProduct.setTag(product);
			btnRemoveProduct.setOnClickListener(this);
		}
		else{
			txtQuantity.setVisibility(View.GONE);
			txtX.setVisibility(View.GONE);
			btnAddProduct.setVisibility(View.GONE);
			btnRemoveProduct.setVisibility(View.GONE);

			if(product.description != null && !product.description.trim().isEmpty()){
				txtDescription.setVisibility(View.VISIBLE);
				txtDescription.setText(product.description);
			}
			else{
				txtDescription.setVisibility(View.GONE);
			}
		}

		return productItem;
	}

	@Override   
	public Filter getFilter() {
		return meuFiltro;   
	}    

	@Override
	public void onClick(View v) {
		int result = 0;
		if(v.getId() == R.id.btnAddProduct){
			result = 1;
		}
		else if(v.getId() == R.id.btnRemoveProduct){
			result = 2;
		}
		
		if(result != 0){
			Intent data = new Intent(receiver);
			data.putExtra(KeysContract.PRODUCT_KEY, (Product) v.getTag());
			data.putExtra(KeysContract.RESULT_KEY, result);
			context.sendBroadcast(data);
		}
	}
	
	public void updateProduct(Product product){
		if(resultados != null){
			int index = resultados.indexOf(product);
			if(index >= 0){
				resultados.set(index, product);
				QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
				app.updateProduct(index, product);
			}
		}
		
		if(productList != null){
			ArrayList<Product> temp = new ArrayList<Product>(productList);
			int index = temp.indexOf(product);
			if(index >= 0){
				temp.set(index, product);
				QuiosgramaApp app = (QuiosgramaApp) context.getApplicationContext();
				app.updateProduct(index, product);
				
				productList = new HashSet<Product>(temp);
			}
		}
		
		notifyDataSetChanged();
	}

	private class MeuFiltro extends Filter {
		private final String[] REPLACES = { "a", "e", "i", "o", "u", "c" };

		private Pattern[] PATTERNS = null;


		@Override
		protected FilterResults performFiltering(CharSequence constraint) {

			FilterResults filterResults = new FilterResults();

			ArrayList<Product> temp = new ArrayList<Product>();

			if (constraint != null) {
				Product p = null;
				
				try{
					long code  = Long.valueOf(constraint.toString());
					p = findByCode(code);
					
					if(p != null)
						temp.add(p);
				}
				catch(NumberFormatException e){
					temp = findByString(constraint);
				}

			}

			filterResults.values = temp;
			filterResults.count = temp.size();
			return filterResults;
		}
		
		private Product findByCode(long code){
			for (Product p : productList) {
				if (code == p.code){
					return p;
				}
			}
			
			return null;
		}
		
		private ArrayList<Product> findByString(CharSequence constraint){
			ArrayList<Product> temp = new ArrayList<Product>();
			
			String term = removeAcentos(
					constraint.toString().trim().toLowerCase());
			
			String placeStr;
			for (Product p : productList) {
				placeStr = removeAcentos(p.name.toLowerCase());
				
				if ( placeStr.indexOf(term) > -1){
					temp.add(p);
				}
			}
			
			return temp;
		}

		private String removeAcentos(String lowerCase) {

			if (PATTERNS == null) {
				compilePatterns();
			}

			String result = lowerCase;
			for (int i = 0; i < PATTERNS.length; i++) {     
				Matcher matcher = PATTERNS[i].matcher(result);     
				result = matcher.replaceAll(REPLACES[i]);   
			}   

			return result.toUpperCase(); 
		}

		public void compilePatterns() {
			PATTERNS = new Pattern[REPLACES.length];
			PATTERNS[0] = Pattern.compile("[âãáàä]", Pattern.CASE_INSENSITIVE);
			PATTERNS[1] = Pattern.compile("[éèêë]", Pattern.CASE_INSENSITIVE);
			PATTERNS[2] = Pattern.compile("[íìîï]", Pattern.CASE_INSENSITIVE);
			PATTERNS[3] = Pattern.compile("[óòôõö]", Pattern.CASE_INSENSITIVE);
			PATTERNS[4] = Pattern.compile("[úùûü]", Pattern.CASE_INSENSITIVE);
			PATTERNS[5] = Pattern.compile("[ç]", Pattern.CASE_INSENSITIVE);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence contraint, FilterResults filterResults) {

			resultados = (ArrayList<Product>) 
					filterResults.values;

			notifyDataSetChanged();
		}

	}

}

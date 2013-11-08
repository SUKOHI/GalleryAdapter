package com.sukohi.lib;

import java.util.ArrayList;
import java.util.concurrent.RejectedExecutionException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class GalleryAdapter extends ArrayAdapter<Bitmap> {

	public static final int PROGRESS_STYLE_NONE = 0;
	public static final int PROGRESS_STYLE_SMALL = 1;
	public static final int PROGRESS_STYLE_MEDIUM = 2;
	public static final int PROGRESS_STYLE_LARGE = 3;
	private final int[] progressStyles = {
			-1,
			android.R.attr.progressBarStyleSmall, 
			android.R.attr.progressBarStyle,
			android.R.attr.progressBarStyleLarge
	};
	private final Context context;
	private ArrayList<Integer> resourceIds;
	private int progressBarStyle = -1;
	
	public GalleryAdapter(Context context, ArrayList<Integer> resourceIds) {
		super(context, 0);
		this.context = context;
		this.resourceIds = resourceIds;
	}

	public void add(int resourceId) {
		
		resourceIds.add(resourceId);
		
	}

	public void add(int resourceId, int index) {
		
		resourceIds.add(index, resourceId);
		
	}

	public void remove(int index) {
		
		resourceIds.remove(index);
		
	}
	
	public void setProgressBarStyle(int styleNumber) {
		
		progressBarStyle = progressStyles[styleNumber];
		
	}
	
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	RelativeLayout relativeLayout = new RelativeLayout(context);
    	ProgressBar progressBar = null;
    	
    	if(progressBarStyle != -1) {
    		
    		progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);
    		relativeLayout.addView(progressBar, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        	
    	}
    	
    	SquareImageView squareImageView = new SquareImageView(context, progressBar);
    	squareImageView.resourceId = resourceIds.get(position);
    	relativeLayout.addView(squareImageView);
    	
        return relativeLayout;
        
    }
    
	@Override
	public int getCount() {
		
		return resourceIds.size();
		
	}
	
	private class SquareImageView extends ImageView {

		private int viewLength, resourceId;
		private Paint paint = new Paint();
		private Rect rect = new Rect();
		private ProgressBar progressBar;
		
		private SquareImageView(Context context, ProgressBar progressBar) {
			super(context);
			this.progressBar = progressBar;
		}
		
		@Override
		public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			viewLength = MeasureSpec.getSize(widthMeasureSpec);
		    setMeasuredDimension(viewLength, viewLength);
		}
		
		@SuppressLint("DrawAllocation")
		@Override
		protected void onDraw(Canvas canvas) {
			
			int canvasLength = canvas.getWidth();
			BitmapDrawable bitamBitmapDrawable = (BitmapDrawable)getDrawable();
			
			if(bitamBitmapDrawable == null) {
				
	        	GridItemTask task = new GridItemTask(this, context.getResources(), canvasLength);
				
			    try {  
			    	task.execute(resourceId);
			    } catch(RejectedExecutionException e){  
				    try{  
				    	task.execute(resourceId);
				    } catch(Exception exception){}  
			    }
			    
			    return;
			    
			}
			
			Bitmap bitmap = bitamBitmapDrawable.getBitmap();
			
			if(bitmap == null) return;
			
			int bitmapWidth = bitmap.getWidth();
			int bitmapHeight = bitmap.getHeight();
			int rectStartX, rectStartY, rectEndX, rectEndY;
			
			if(bitmapWidth == bitmapHeight) {
				
				if(bitmapWidth != canvasLength) {
					
					bitmap = Bitmap.createScaledBitmap(bitmap, canvasLength, canvasLength, false);
					
				}
				
				rectStartX = rectStartY = 0;
				rectEndX = rectEndY = canvasLength;
				
			} else {
				
				float ratio;
				
				if(bitmapWidth > bitmapHeight) {
					
					ratio = (float)bitmapHeight / (float)bitmapWidth;
					bitmap = Bitmap.createScaledBitmap(bitmap, canvasLength, (int)(ratio*canvasLength), false);
					rectStartX = 0;
					rectStartY = (int) ((float)canvasLength / 2F - (float)bitmap.getHeight() / 2F);
					rectEndX = canvasLength;
					rectEndY = rectStartY + bitmap.getHeight();
					
				} else {
					
					ratio = (float)bitmapWidth / (float)bitmapHeight;
					bitmap = Bitmap.createScaledBitmap(bitmap, (int)(ratio*canvasLength), canvasLength, false);
					rectStartX = rectStartY = (int) ((float)canvasLength / 2F - (float)bitmap.getWidth() / 2F);
					rectStartY = 0;
					rectEndX = rectStartX + bitmap.getWidth();
					rectEndY = canvasLength;
					
				}
				
			}
			
			rect.set(rectStartX, rectStartY, rectEndX, rectEndY);
			canvas.drawBitmap(bitmap, null, rect, paint);
			bitmap.recycle();
			
		}
		
	}
	
    private class GridItemTask extends AsyncTask<Integer, Void, Bitmap> {
    	
        private SquareImageView squareImageView;
        private Resources resources;
        private int canvasLength;
     
        public GridItemTask(SquareImageView squareImageView, Resources resources, int canvasLength) {
        	this.squareImageView = squareImageView;
        	this.resources = resources;
        	this.canvasLength = canvasLength;
        }
        
        @Override
        protected void onPostExecute(Bitmap bitmap) {
        	
        	if(squareImageView.progressBar != null) {
        		
            	squareImageView.progressBar.setVisibility(View.GONE);
            	
        	}
        	
        	squareImageView.setImageBitmap(bitmap);
        }

		@Override
		protected Bitmap doInBackground(Integer... params) {
			
			int resourceId = params[0];
			
			BitmapFactory.Options options = new BitmapFactory.Options();  
			options.inJustDecodeBounds = true;
			options.inPurgeable = true;
			BitmapFactory.decodeResource(resources, resourceId, options);
			
			int bitmapWidth = options.outWidth;
			int bitmapHeight = options.outHeight;
			int inSampleSize = 1;
			
			if(bitmapWidth > canvasLength || bitmapHeight > canvasLength) {  
			
				if(bitmapWidth > bitmapHeight) {
					
					inSampleSize = (int) Math.floor((float)bitmapWidth / (float)canvasLength);
					
				} else {
					
					inSampleSize = (int) Math.floor((float)bitmapHeight / (float)canvasLength);
					
				}
			
			}
			
		    options.inSampleSize = inSampleSize;
		    options.inJustDecodeBounds = false;
		    return BitmapFactory.decodeResource(resources, resourceId, options);
			
		}
        
    }
    
}
/***Sample

	ArrayList<Integer> resourceIds = new ArrayList<Integer>();
	resourceIds.add(R.drawable.drawable0);
	resourceIds.add(R.drawable.drawable1);
	resourceIds.add(R.drawable.drawable2);
	
	GalleryAdapter galleryAdapter = new GalleryAdapter(this, resourceIds);
	galleryAdapter.setProgressBarStyle(GalleryAdapter.PROGRESS_STYLE_SMALL);	// Or you can use GalleryAdapter.PROGRESS_STYLE_MEDIUM and GalleryAdapter.PROGRESS_STYLE_LARGE
	
	GridView gridView = (GridView) findViewById(R.id.gridview);
	gridView.setAdapter(galleryAdapter);
	
	// Add and remove when click item

	gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			galleryAdapter.add(R.drawable.new_drawable);			// Add
			galleryAdapter.add(R.drawable.new_drawable, position);	// Add with position
			galleryAdapter.remove(position);						// Remove
			gridView.invalidateViews();	// Refresh
			
		}
		
	});

***/

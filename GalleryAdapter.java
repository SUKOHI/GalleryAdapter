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
import android.util.Pair;
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
	private final int IMAGE_TYPE_RESOURCE_ID = 1;
	private final int IMAGE_TYPE_BITMAP = 2;
	private final int[] progressStyles = {
			-1,
			android.R.attr.progressBarStyleSmall, 
			android.R.attr.progressBarStyle,
			android.R.attr.progressBarStyleLarge
	};
	private final Context context;
	private ArrayList<Integer> resourceIds;
	private ArrayList<Bitmap> bitmaps;
	private int progressBarStyle = 2;
	private ArrayList<Pair<Integer, Integer>> imageValues = new ArrayList<Pair<Integer,Integer>>();
	
	public GalleryAdapter(Context context) {
		super(context, 0);
		this.context = context;
		resourceIds = new ArrayList<Integer>();
		bitmaps = new ArrayList<Bitmap>();
	}
	
	public void addImageResourceIds(ArrayList<Integer> resourceIds) {
		
		for(int i = 0 ; i < resourceIds.size() ; i++) {
			
			addImageResourceId(resourceIds.get(i));
			
		}
		
	}
	
	public void addImageBitmaps(ArrayList<Bitmap> bitmaps) {

		for(int i = 0 ; i < bitmaps.size() ; i++) {
			
			addImageBitmap(bitmaps.get(i));
			
		}
		
	}
	
	public void addImageResourceId(int resourceId) {
		
		imageValues.add(new Pair<Integer, Integer>(IMAGE_TYPE_RESOURCE_ID, resourceIds.size()));
		resourceIds.add(resourceId);
		
	}

	public void addImageResourceId(int resourceId, int index) {
		
		int addIndex = getAddIndex(IMAGE_TYPE_RESOURCE_ID, index, resourceIds.size());
		imageValues.add(index, new Pair<Integer, Integer>(IMAGE_TYPE_RESOURCE_ID, addIndex));
		resourceIds.add(addIndex, resourceId);
		
	}
	
	public void addImageBitmap(Bitmap bitmap) {
		
		imageValues.add(new Pair<Integer, Integer>(IMAGE_TYPE_BITMAP, bitmaps.size()));
		bitmaps.add(bitmap);
		
	}

	public void addImageBitmap(Bitmap bitmap, int index) {
		
		int addIndex = getAddIndex(IMAGE_TYPE_BITMAP, index, bitmaps.size());
		imageValues.add(index, new Pair<Integer, Integer>(IMAGE_TYPE_BITMAP, addIndex));
		bitmaps.add(addIndex, bitmap);
		
	}

	private int getAddIndex(int imageType, int index, int defaultIndex) {
		
		boolean firstImageValueFlag = true;
		Pair<Integer, Integer> imageValue;
		int imageValueType, imageValueIndex;
		int addIndex = -1;
		
		for(int i = 0; i < imageValues.size(); i++) {
			
			imageValue = imageValues.get(i);
			imageValueType = imageValue.first;
			imageValueIndex = imageValue.second;
			
			if(imageType == imageValueType && i >= index) {
				
				if(firstImageValueFlag) {
					
					addIndex = imageValueIndex;
					firstImageValueFlag = false;
					
				}
				
				imageValues.set(i, new Pair<Integer, Integer>(imageValueType, (imageValueIndex+1)));
				
			}
			
		}
		
		if(addIndex == -1) {
			
			return defaultIndex;
			
		}
		
		return addIndex;
		
	}
	
	public void replaceImageResourceId(int resourceId, int index) {
		
		remove(index);
		addImageResourceId(resourceId, index);
		
	}
	
	public void replaceImageBitmap(Bitmap bitmap, int index) {
		
		remove(index);
		addImageBitmap(bitmap, index);
		
	}
	
	public void remove(int index) {
		
		Pair<Integer, Integer> imageValue = imageValues.get(index);
		int imageType = imageValue.first;
		int imageIndex = imageValue.second;
		
		switch(imageType) {
		case IMAGE_TYPE_RESOURCE_ID:
			resourceIds.remove(imageIndex);
			break;
		case IMAGE_TYPE_BITMAP:
			bitmaps.remove(imageIndex);
			break;
		}
		
		imageValues.remove(index);
		
		for(int i = index; i < imageValues.size(); i++) {
			
			imageValue = imageValues.get(i);
			
			if(imageType == imageValue.first) {
				
				imageValues.set(i, new Pair<Integer, Integer>(imageValue.first, (imageValue.second-1)));
				
			}
			
		}
		
	}
	
	public void removeAll() {
		
		bitmaps = new ArrayList<Bitmap>();
		resourceIds = new ArrayList<Integer>();
		imageValues = new ArrayList<Pair<Integer,Integer>>();
		
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
    	Pair<Integer, Integer> imageValue = imageValues.get(position);
		int imageType = imageValue.first;
		int imageIndex = imageValue.second;
    	
		if(imageType == IMAGE_TYPE_BITMAP) {
			
			squareImageView.setImageBitmap(bitmaps.get(imageIndex));
			
		} else {
			
			squareImageView.resourceId = resourceIds.get(imageIndex);
			
		}
		
    	relativeLayout.addView(squareImageView);
        return relativeLayout;
        
    }
    
	@Override
	public int getCount() {
		
		return imageValues.size();
		
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
					bitmap = Bitmap.createScaledBitmap(bitmap, canvasLength, (int) Math.ceil(ratio*canvasLength), false);
					rectStartX = 0;
					rectStartY = (int) ((float)canvasLength / 2F - (float)bitmap.getHeight() / 2F);
					rectEndX = canvasLength;
					rectEndY = rectStartY + bitmap.getHeight();
					
				} else {
					
					ratio = (float)bitmapWidth / (float)bitmapHeight;
					bitmap = Bitmap.createScaledBitmap(bitmap, (int) Math.ceil(ratio*canvasLength), canvasLength, false);
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

	GalleryAdapter galleryAdapter = new GalleryAdapter(this);

	// Resource ID

	ArrayList<Integer> resourceIds = new ArrayList<Integer>();
	resourceIds.add(R.drawable.drawable0);
	resourceIds.add(R.drawable.drawable1);
	resourceIds.add(R.drawable.drawable2);
	galleryAdapter.addImageResourceIds(resourceIds);
	
	// Bitmap
	
	ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
	resourceIds.add(bitmap0);
	resourceIds.add(bitmap1);
	resourceIds.add(bitmap2);
	galleryAdapter.addImageBitmaps(bitmaps);
	
	// Progress style: PROGRESS_STYLE_NONE, PROGRESS_STYLE_SMALL, PROGRESS_STYLE_MEDIUM, PROGRESS_STYLE_LARGE
	
	galleryAdapter.setProgressBarStyle(GalleryAdapter.PROGRESS_STYLE_MEDIUM);	// Skippable (Default: PROGRESS_STYLE_MEDIUM)
	
	
	GridView gridView = (GridView) findViewById(R.id.gridview);
	gridView.setAdapter(galleryAdapter);
	
	// Add and remove when click item

	gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			galleryAdapter.addImageResourceId(R.drawable.new_drawable);					// Add using resource ID
			galleryAdapter.addImageResourceId(R.drawable.new_drawable, position);		// Add using resource ID with position
			galleryAdapter.addImageBitmap(bitmap);										// Add bitmap
			galleryAdapter.addImageBitmap(bitmap, position);							// Add bitmap with position
			galleryAdapter.replaceImageResourceId(R.drawable.new_drawable, position);	// Replace
			galleryAdapter.replaceImageBitmap(bitmap, position);						// Replace
			galleryAdapter.remove(position);											// Remove
			galleryAdapter.removeAll();													// Remove all
			gridView.invalidateViews();	// Refresh GridView
			
		}
		
	});

***/

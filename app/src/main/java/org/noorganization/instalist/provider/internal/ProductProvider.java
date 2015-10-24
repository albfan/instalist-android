package org.noorganization.instalist.provider.internal;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.provider.InstalistProvider;
import org.noorganization.instalist.utils.SQLiteUtils;
import org.noorganization.instalist.view.utils.ProviderUtils;

/**
 * Provider for the products used in the application.
 * Created by Tino on 24.10.2015.
 */
public class ProductProvider implements IInternalProvider {
    //region private attributes
    private SQLiteDatabase mDatabase;
    private UriMatcher mMatcher;
    private Context mContext;


    private static final int SINGLE_PRODUCT = 1;
    private static final int MULTIPLE_PRODUCTS = 2;

    private static final String SINGLE_PRODUCT_STRING = "product/*";
    private static final String MULTIPLE_PRODUCTS_STRING = "product";

    //endregion private attributes

    //region public attributes
    public static final String SINGLE_PRODUCT_CONTENT_URI = InstalistProvider.BASE_CONTENT_URI + "/" + SINGLE_PRODUCT_STRING;
    public static final String MULTIPLE_PRODUCT_CONTENT_URI = InstalistProvider.BASE_CONTENT_URI + "/" + MULTIPLE_PRODUCTS_STRING;

    public static final String PRODUCT_BASE_TYPE = "vnd.noorganization.product";
    //endregion public attributes

    //region constructors

    /**
     * Constructor of {@link ProductProvider}
     *
     * @param _context the context of the parent provider. (Needed to notify listener for changes)
     */
    public ProductProvider(Context _context) {
        mContext = _context;
    }
    //endregion constructors

    //region public overriden methods
    @Override
    public void onCreate(SQLiteDatabase _db) {
        mDatabase = _db;
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        mMatcher.addURI(InstalistProvider.AUTHORITY, SINGLE_PRODUCT_STRING, SINGLE_PRODUCT);
        mMatcher.addURI(InstalistProvider.AUTHORITY, MULTIPLE_PRODUCTS_STRING, MULTIPLE_PRODUCTS);
    }

    @Override
    public Cursor query(@NonNull Uri _uri, String[] _projection, String _selection, String[] _selectionArgs, String _sortOrder) {
        Cursor cursor = null;
        switch (mMatcher.match(_uri)) {

            case SINGLE_PRODUCT:
                String selection = ProviderUtils.getSelectionWithIdQuery(Product.COLUMN_ID, _selection);
                String[] selectionArgs = ProviderUtils.getSelectionArgsWithId(_selectionArgs, _uri.getLastPathSegment());
                cursor = mDatabase.query(Product.TABLE_NAME, _projection, selection, selectionArgs, null, null, _sortOrder);
                break;
            case MULTIPLE_PRODUCTS:
                cursor = mDatabase.query(Product.TABLE_NAME, _projection, _selection, _selectionArgs, null, null, _sortOrder);
                break;
            default:
                throw new IllegalArgumentException("The given Uri is not supported: " + _uri);
        }

        if (cursor != null) {
            // notify all possibles listener
            cursor.setNotificationUri(mContext.getContentResolver(), _uri);
        }

        return cursor;
    }

    @Override
    public String getType(@NonNull Uri _uri) {
        switch (mMatcher.match(_uri)) {
            case SINGLE_PRODUCT:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + PRODUCT_BASE_TYPE;
            case MULTIPLE_PRODUCTS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + PRODUCT_BASE_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(@NonNull Uri _uri, ContentValues _values) {
        Uri newUri = null;
        switch (mMatcher.match(_uri)) {
            case SINGLE_PRODUCT:
                long rowId = mDatabase.insert(Product.TABLE_NAME, null, _values);
                // insertion went wrong
                if (rowId == -1) {
                    throw new SQLiteException("Failed to add a record into " + _uri);
                }
                Cursor cursor = mDatabase.query(Product.TABLE_NAME, new String[]{Product.COLUMN_ID},
                        SQLiteUtils.COLUMN_ROW_ID + "=?", new String[]{String.valueOf(rowId)},
                        null, null, null, null);
                cursor.moveToFirst();
                newUri = Uri.parse(SINGLE_PRODUCT_CONTENT_URI.replace("*",
                        cursor.getString(cursor.getColumnIndex(Product.COLUMN_ID))));
                break;
            case MULTIPLE_PRODUCTS:
                // TODO: implement!
                newUri = null; //Uri.parse(MULTIPLE_PRODUCT_CONTENT_URI);
                break;
            default:
                throw new IllegalArgumentException("The given Uri is not supported: " + _uri);
        }

        if (newUri != null) {
            mContext.getContentResolver().notifyChange(_uri, null);
        }
        return newUri;
    }

    @Override
    public int delete(@NonNull Uri _uri, String _selection, String[] _selectionArgs) {
        int affectedRows = 0;

        switch (mMatcher.match(_uri)) {
            case SINGLE_PRODUCT:
                String selection = ProviderUtils.getSelectionWithIdQuery(Product.COLUMN_ID, null);
                String[] selectionArgs = ProviderUtils.getSelectionArgsWithId(null, _uri.getLastPathSegment());
                affectedRows = mDatabase.delete(Product.TABLE_NAME, selection, selectionArgs);
                break;
            case MULTIPLE_PRODUCTS:
                affectedRows = mDatabase.delete(Product.TABLE_NAME, _selection, _selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("The given Uri is not supported: " + _uri);
        }
        if (affectedRows > 0) {
            mContext.getContentResolver().notifyChange(_uri, null);
        }
        return affectedRows;
    }

    @Override
    public int update(@NonNull Uri _uri, ContentValues _values, String _selection, String[] _selectionArgs) {
        int affectedRows = 0;
        switch (mMatcher.match(_uri)) {
            case SINGLE_PRODUCT:
                String selection = ProviderUtils.getSelectionWithIdQuery(Product.COLUMN_ID, null);
                String[] selectionArgs = ProviderUtils.getSelectionArgsWithId(null, _uri.getLastPathSegment());
                affectedRows = mDatabase.update(Product.TABLE_NAME, _values, selection, selectionArgs);
                break;
            case MULTIPLE_PRODUCTS:
                affectedRows = mDatabase.update(Product.TABLE_NAME, _values, _selection, _selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("The given Uri is not supported: " + _uri);
        }
        if (affectedRows > 0) {
            mContext.getContentResolver().notifyChange(_uri, null);
        }
        return affectedRows;
    }

    //endregion overwritten methods
}

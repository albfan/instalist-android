package org.noorganization.instalist.presenter.touchlistener.sidebar;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.noorganization.instalist.R;
import org.noorganization.instalist.controller.ICategoryController;
import org.noorganization.instalist.controller.implementation.ControllerFactory;
import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.presenter.interfaces.IBaseActivity;
import org.noorganization.instalist.presenter.listadapter.ExpandableCategoryItemListAdapter;
import org.noorganization.instalist.presenter.utils.ViewUtils;

/**
 * Handle the validation and processing when the submit button of category editing was pressed.
 * Created by TS on 20.06.2015.
 */
public class OnSubmitClickListenerWithParentData implements View.OnClickListener {

    private ViewSwitcher                      mViewSwitcher;
    private EditText                          mNameEditText;
    private String                            mCategoryId;
    private ExpandableCategoryItemListAdapter mAdapter;
    private ICategoryController               mCategoryController;

    public OnSubmitClickListenerWithParentData(Context _context, ViewSwitcher _ViewSwitcher, EditText _NameEditText,
                                               String _CategoryId, ExpandableCategoryItemListAdapter _Adapter) {
        mViewSwitcher = _ViewSwitcher;
        mCategoryId = _CategoryId;
        mNameEditText = _NameEditText;
        mAdapter = _Adapter;
        mCategoryController = ControllerFactory.getCategoryController(_context);
    }

    @Override
    public void onClick(View _View) {
        if (! ViewUtils.checkEditTextIsFilled(mNameEditText)) {
            return;
        }

        String   insertedText = mNameEditText.getText().toString();
        Category oldCategory  = mAdapter.findCategoryById(mCategoryId);
        Category newCategory  = mCategoryController.renameCategory(oldCategory, insertedText);
        Context  context      = _View.getContext();

        if (newCategory == null) {
            Toast.makeText(context, _View.getContext().getString(R.string.category_not_found), Toast.LENGTH_SHORT).show();
            return;
        } else if (newCategory.equals(oldCategory)) {
            if (newCategory.mName.compareTo(insertedText) != 0) {
                Toast.makeText(context, context.getString(R.string.category_exists), Toast.LENGTH_SHORT).show();
                mNameEditText.setError(context.getString(R.string.category_exists));
                return;
            }
        }
        ViewUtils.removeSoftKeyBoard(context, mNameEditText);
        mViewSwitcher.showNext();
        // TODO: remove this when callback for this kind is there.
        ((IBaseActivity) context).updateCategory(newCategory);


    }
}
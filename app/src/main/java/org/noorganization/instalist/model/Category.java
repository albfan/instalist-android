package org.noorganization.instalist.model;

import com.orm.StringUtil;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

/**
 * Representation of a category.
 * Created by daMihe on 25.05.2015.
 */
public class Category extends SugarRecord<Category> {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";

    public static final String TABLE_NAME = "category";

    public static final String DB_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_ID + " TEXT PRIMARY KEY NOT NULL, " +
            COLUMN_NAME + " TEXT NOT NULL)";

    public static final String ATTR_NAME = StringUtil.toSQLName("mName");

    public String mName;

    public Category() {
    }

    public Category(String _name) {
        mName = _name;
    }

    public List<ShoppingList> getLists() {
        return Select.from(ShoppingList.class).where(
                Condition.prop(ShoppingList.ATTR_CATEGORY).eq(getId())).list();
    }

    public static List<ShoppingList> getListsWithoutCategory(){
        // WTF?! 0 as null value?! This was not documented in SugarORM's doc and is really weird.
        return Select.from(ShoppingList.class).where(Condition.prop(ShoppingList.ATTR_CATEGORY).eq(0)).list();
    }
    @Override
    public boolean equals(Object _another) {
        if (_another == this) {
            return true;
        }

        if (_another == null ||_another.getClass() != getClass()) {
            return false;
        }

        Category anotherCategory = (Category) _another;

        return (getId().compareTo(anotherCategory.getId()) == 0 && mName.equals(anotherCategory.mName));
    }

    @Override
    public int hashCode() {
        return getId().intValue();
    }
}

package org.noorganization.instalist.controller.event;

import org.noorganization.instalist.model.Product;

/**
 * This event will be fired by teh controller if a Product gets changed.
 * Created by daMihe on 19.07.2015.
 */
public class ProductChangedMessage {
    public Change  mChange;
    public Product mProduct;

    public ProductChangedMessage(Change _change, Product _product) {
        mChange  = _change;
        mProduct = _product;
    }
}

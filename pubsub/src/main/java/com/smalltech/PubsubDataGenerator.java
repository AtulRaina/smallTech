package com.smalltech;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class PubsubDataGenerator {


    public PubsubDataGenerator() {

        timeofsale=new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        merchant=get_random_element(merchantList);
        productid=get_random_element(productidList);
        sellingPrice=get_random_element(sellingPriceList);
        quantity=get_random_element(quantityList);

    }
    private String get_random_element(List<String> givenList)
    {
        Random rand = new Random();
        int randomIndex = rand.nextInt(givenList.size());
        return  givenList.get(randomIndex);
    }
    public String getTimeofsale() {
        return timeofsale;
    }

    public void setTimeofsale(String timeofsale) {
        this.timeofsale = timeofsale;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }

    public String getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(String sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    String timeofsale;
    String merchant;
    String productid;
    String sellingPrice;
    String quantity;
    List<String> productidList = Arrays.asList("2e5da385-c96b-402e-bfc9-6366f84aacb8",
            "bc96e48c-07ff-4544-a488-72fc1ea49e58",
            "000a0f96-c913-4a0b-886b-6ce8f8837e48",
            "3d19f728-5ba1-4f2a-8701-94fd11925cbd");
    List<String> merchantList = Arrays.asList("coles", "aldi", "iga", "woolworth");
    List<String> sellingPriceList = Arrays.asList("20", "10", "10", "12");
    List<String> quantityList = Arrays.asList("1", "2", "3", "5","6","8","10");
}

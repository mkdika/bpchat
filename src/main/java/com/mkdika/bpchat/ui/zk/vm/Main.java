package com.mkdika.bpchat.ui.zk.vm;

import org.zkoss.bind.annotation.Init;

/**
 *
 * @author Maikel Chandika <mkdika@gmail.com>
 */
public class Main {
    
    private String nama = "Maikel Chandika";
    
    @Init        
    public void init() {        
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }
    
    
    
}

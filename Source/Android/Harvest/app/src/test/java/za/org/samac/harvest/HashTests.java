package za.org.samac.harvest;

import org.junit.Test;

import za.org.samac.harvest.util.AppUtil;

import static org.junit.Assert.*;

public class HashTests {

    @Test
    public void HashTest(){
        assertTrue(AppUtil.Hash.SHA256("Hello World").equals("a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e"));
    }
}

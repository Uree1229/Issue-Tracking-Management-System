package com.example.its;

import com.example.its.common.util.DatabaseInitializer;

public class ItsApplication {

    public static void main(String[] args) {
        DatabaseInitializer.resetDatabaseAndSeed();
        System.out.println("DB 초기화 및 seed data 삽입이 완료되었습니다.");
    }
}

package com.eximbay.payoutbatch.component;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Slf4j
@Component
public class FirmBanking {

    /// Fields
    private Socket socket;
    private InputStream is;
    private OutputStream os;

    @Value("${firm.banking.host}")
    private String host;

    @Value("${firm.banking.port}")
    private String port;
    /// Constructor

    /// Method
    public void checkAccountNumber() {

    }

}

/**
 * 업무망 프로젝트 이전 이후 작업할 내용
 */
package com.project.tickets.config;

import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QrCodeConfig {

    @Bean
    public QRCodeWriter qrCodeWriter() {
        return new QRCodeWriter();
    }
}

//QR Code Setup
//1.Add dependencies : Qr code generation dependencies : zxing core and javase
//2.Make config fie and add method QRCodeWriter
//3.Make sepratae Repo,Service,Controller for this

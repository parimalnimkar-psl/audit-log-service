package com.example.audit.service;
import java.nio.charset.StandardCharsets; import java.security.*; import org.springframework.stereotype.Service;
@Service public class HashService { public String hash(String value){ try{ byte[] b=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder s=new StringBuilder(); for(byte x:b)s.append(String.format("%02x",x)); return s.toString(); }catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);} }
 public String canonical(long seq,String type,String actor,String rt,String rid,String payload,java.time.Instant ts){ return String.join("|","v1",Long.toString(seq),type,actor,rt,rid,payload,ts.toString()); } }

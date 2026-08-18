package com.example.audit.service;
import java.nio.charset.StandardCharsets; import java.security.*; import org.springframework.stereotype.Service;
@Service public class HashService { public String hash(String value){ try{ byte[] b=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)); StringBuilder s=new StringBuilder(); for(byte x:b)s.append(String.format("%02x",x)); return s.toString(); }catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);} }
 public String canonical(long seq,String type,String actor,String rt,String rid,String payload,java.time.Instant ts){ 
        // Escape pipe and backslash to prevent delimiter injection attacks
        String v1 = escape("v1");
        String s = escape(Long.toString(seq));
        String t = escape(type);
        String a = escape(actor);
        String r = escape(rt);
        String ri = escape(rid);
        String p = escape(payload);
        String tss = escape(ts.toString());
        return String.join("|", v1, s, t, a, r, ri, p, tss);
    }
    
    private String escape(String value) {
        if (value == null) return "";
        // Escape backslash first, then pipe to prevent double-escaping issues
        return value.replace("\\", "\\\\").replace("|", "\\|");
    } }

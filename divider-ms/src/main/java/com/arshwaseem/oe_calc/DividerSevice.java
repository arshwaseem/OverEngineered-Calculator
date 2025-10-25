package com.arshwaseem.oe_calc;

import com.arshwaseem.oe_calc.exception.DivideException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DividerSevice implements DividerUseCases {
    private static final Logger log = LoggerFactory.getLogger(DividerSevice.class);

    public double Divider(double numA, double numB) throws DivideException {
        try{
            if(numB == 0){
                throw new DivideException("Attempted to divide by zero, division failed");
            }
            return numA/numB;
        } catch (DivideException e){
            log.error("Divide by zero exception: {}", e.getMessage());
            return 0;
        } catch (Exception e){
            log.error("An error occured while dividing {}", e.getMessage());
            return 0;
        }
    }
}

package it.openly.core.test.pojos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class CoolPerson {
    Integer idx;
    String firstName;
    String lastName;
    Date subscriptionDate;
    BigDecimal rating;
}

package et.kacha.interestcalculating.entity;

import et.kacha.interestcalculating.constants.SubscriptionStatus;
import et.kacha.interestcalculating.constants.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Table(name = "subscriptions")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Subscriptions {

    @Id
    @GeneratedValue(generator = "subscriptions_id_seq", strategy = GenerationType.IDENTITY)
    @Column(insertable = false)
    private Integer id;

    @Column(nullable = false)
    private Float balance;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String account_number;

    @Column(nullable = false)
    private String short_code;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customers customer;

    @ManyToOne
    @JoinColumn(nullable = false, name = "saving_product_id")
    private Products product;

    //ACTIVE,INACTIVE
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    @CreationTimestamp
    @Column(nullable = false)
    private Date created_at;

    @UpdateTimestamp
    @Column(nullable = false)
    private Date updated_at;

}

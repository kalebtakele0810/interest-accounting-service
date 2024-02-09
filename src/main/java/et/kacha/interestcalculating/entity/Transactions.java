package et.kacha.interestcalculating.entity;

import et.kacha.interestcalculating.constants.InterestPaymentState;
import et.kacha.interestcalculating.constants.TransactionStatus;
import et.kacha.interestcalculating.constants.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.Date;

@Table(name = "ledgers")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Transactions {

    @Id
    @GeneratedValue(generator = "ledgers_id_seq", strategy = GenerationType.IDENTITY)
    @Column(insertable = false)
    private Integer id;

    @Column(nullable = false)
    private float amount;

    @Column(nullable = false)
    private float balance;

    @Enumerated(EnumType.STRING) // You can choose EnumType.ORDINAL for integer representation
    private TransactionType transaction_type;

    @Column(nullable = false)
    private String txn_id;

    @Column(nullable = false)
    private String txn_ref;

    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private Subscriptions subscriptions;

    ///PENDING,SUCCESS,FAILED
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @CreationTimestamp
    private Date created_at;

    @UpdateTimestamp
    private Date updated_at;

}

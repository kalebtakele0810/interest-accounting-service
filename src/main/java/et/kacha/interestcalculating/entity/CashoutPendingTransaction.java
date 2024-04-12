package et.kacha.interestcalculating.entity;

import et.kacha.interestcalculating.constants.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Table(name = "cashout_pending_transactions")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CashoutPendingTransaction {

    @Id
    @GeneratedValue(generator = "cashout_pending_transactions_id_seq", strategy = GenerationType.IDENTITY)
    @Column(insertable = false)
    private Integer id;

    @Column(nullable = false)
    private Float amount;

    @Column(nullable = false)
    private String kacha_reference;

    @Column
    private Integer subscription_id;

    @Column
    private Integer agent_id;

    @Column
    private Integer ledger_id;

    //ACTIVE,DISABLED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private USSDStatus status;

    @CreationTimestamp
    @Column(nullable = false)
    private Date created_at;

    @UpdateTimestamp
    @Column(nullable = false)
    private Date updated_at;
}

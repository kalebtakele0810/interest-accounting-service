package et.kacha.interestcalculating.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.Date;

@Table(name = "transactions")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Transactions {

    @Id
    @GeneratedValue(generator = "Transactions_sequence", strategy = GenerationType.IDENTITY)
    @Column(insertable = false)
    private Integer id;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private int balance;

    @ManyToOne
    private Customers customer;

    @ManyToOne
    private Products product;

    @CreationTimestamp
    private Date created_at_time;

    @UpdateTimestamp
    private Date updated_at_time;
}

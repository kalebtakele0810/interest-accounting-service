package et.kacha.interestcalculating.entity;

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
    private float balance;

    @Column(nullable = false)
    private String phone;

    @ManyToOne
    private Customers customer;

    @ManyToOne
    private Products product;

    @CreationTimestamp
    private Date created_at;

    @UpdateTimestamp
    private Date updated_at;

}

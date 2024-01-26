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
    @GeneratedValue(generator = "Subscriptions_sequence", strategy = GenerationType.IDENTITY)
    @Column(insertable = false)
    private Integer id;

    @ManyToOne
    private Customers customer;

    @ManyToOne
    private Products product;

    @CreationTimestamp
    private Date created_at_time;

    @UpdateTimestamp
    private Date updated_at_time;

}

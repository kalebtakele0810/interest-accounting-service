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

@Table(name = "whitelists")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Whitelist {

    @Id
    @GeneratedValue(generator = "whitelist_id_seq", strategy = GenerationType.IDENTITY)
    @Column(insertable = false)
    private Integer id;

    @Column(nullable = false)
    private String phone;

    @ManyToOne
    @JoinColumn(name = "saving_product_id")
    private Products products;

    @Column(nullable = false)
    private Integer fi_id;

    @Column(nullable = false)
    private Integer added_by;

    //ACTIVE,PENDING,INACTIVE,DECLINED
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChargeState status;

    @Column
    private Integer approved_by;

    @CreationTimestamp
    @Column(nullable = false)
    private Date created_at;

    @UpdateTimestamp
    @Column(nullable = false)
    private Date updated_at;

}

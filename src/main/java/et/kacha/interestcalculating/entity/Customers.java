package et.kacha.interestcalculating.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Table(name = "customers")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Customers {

    @Id
    @GeneratedValue(generator = "customers_id_seq", strategy = GenerationType.IDENTITY)
    @Column(insertable = false)
    private Integer id;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String middlename;

    @Column(nullable = false)
    private String lastname;

    @Column(nullable = false)
    private String phone;

    @CreationTimestamp
    private Date created_at;

    @UpdateTimestamp
    private Date updated_at;
}

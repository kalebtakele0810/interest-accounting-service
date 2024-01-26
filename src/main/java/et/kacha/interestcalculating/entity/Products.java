package et.kacha.interestcalculating.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.Date;

@Table(name = "products")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Products {

    @Id
    @GeneratedValue(generator = "Products_sequence", strategy = GenerationType.IDENTITY)
    @Column(insertable = false)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String duration;

    @Column(nullable = false)
    private float rate;

    @Column(nullable = false)
    private String status;

    @CreationTimestamp
    private Date created_at_time;

    @UpdateTimestamp
    private Date updated_at_time;

}

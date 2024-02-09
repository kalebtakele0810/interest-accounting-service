package et.kacha.interestcalculating.entity;

import et.kacha.interestcalculating.constants.InterestPaymentState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Table(name = "interest_tax_history")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class TaxHistory {
    @Id
    @GeneratedValue(generator = "interest_tax_history_sequence", strategy = GenerationType.IDENTITY)
    @Column(insertable = false)
    private Integer id;

    @ManyToOne
    private InterestHistory interestHistory;

    private double amount;

    @Enumerated(EnumType.STRING) // You can choose EnumType.ORDINAL for integer representation
    private InterestPaymentState status;

    @CreationTimestamp
    private Date created_at;

    @UpdateTimestamp
    private Date updated_at;
}
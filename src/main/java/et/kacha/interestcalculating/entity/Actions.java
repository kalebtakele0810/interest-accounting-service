package et.kacha.interestcalculating.entity;

import et.kacha.interestcalculating.constants.ChargeState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Table(name = "actions")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Actions {

    @Id
    @GeneratedValue(generator = "actions_id_seq", strategy = GenerationType.IDENTITY)
    @Column(insertable = false)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String action;

    @Column
    private String sub_action;

    @Column
    private Integer user_id;

    //ACTIVE,INACTIVE
    @Enumerated(EnumType.STRING)
    private ChargeState status;

    @CreationTimestamp
    @Column(nullable = false)
    private Date created_at;

    @UpdateTimestamp
    @Column(nullable = false)
    private Date updated_at;

}

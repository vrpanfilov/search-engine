package main.model;

import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Table(name = "`index`")
@Data
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false, foreignKey=@ForeignKey(name = "FK_index_page"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Page page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", nullable = false, foreignKey=@ForeignKey(name = "FK_index_lemma"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Lemma lemma;

    @Override
    public int hashCode() {
        return page.hashCode() + lemma.hashCode();
    }

    @Column(name = "`rank`", nullable = false)
    private float rank;

    public Index() {

    }

    public Index(Page page, Lemma lemma, float rank) {
        this.page = page;
        this.lemma = lemma;
        this.rank = rank;
    }

    @Override
    public String toString() {
        return "id: " + id + "; page: " + page.getPath() + "; lemma: " + lemma.getLemma();
    }
}

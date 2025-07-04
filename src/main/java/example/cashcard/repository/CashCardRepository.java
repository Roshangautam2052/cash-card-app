package example.cashcard.repository;

import example.cashcard.model.CashCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CashCardRepository extends CrudRepository<CashCard, Long>, PagingAndSortingRepository<CashCard, Long> {

    CashCard findByIdAndOwner(Long id, String owner);

    Boolean existsByIdAndOwner(Long id, String owner);

    Page<CashCard> findByOwner(String owner, PageRequest pageRequest);

}

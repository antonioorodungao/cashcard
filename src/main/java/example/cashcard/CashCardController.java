package example.cashcard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {

    @Autowired
    private CashCardRepository cashCardRepository;

    private CashCard findCashCard(Long requestId, Principal principal){
        return cashCardRepository.findByIdAndOwner(requestId, principal.getName());
    }

    public CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{requestedId}")
    public ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {

        Optional<CashCard> cashCardOptional = Optional.ofNullable(findCashCard(requestedId, principal));
        if (cashCardOptional.isPresent()) {
            return ResponseEntity.ok(cashCardOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @PostMapping
    public ResponseEntity createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb, Principal principal) throws URISyntaxException {
        CashCard cashCardwithOwner = new CashCard(null, newCashCardRequest.amount(), principal.getName());
        CashCard savedCashCard = cashCardRepository.save(cashCardwithOwner);
        URI uriLocation = ucb.path("/cashcards/{id}").buildAndExpand(savedCashCard.id()).toUri();
        return ResponseEntity.created(uriLocation).build();
    }

    @GetMapping
    public ResponseEntity<Iterable<CashCard>> findAll(Pageable pageable, Principal principal) {
//        Page<CashCard> page = cashCardRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
//                pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))));

        Page<CashCard> page = cashCardRepository.findByOwner(principal.getName(), PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))));
        return ResponseEntity.ok(page.getContent());
    }
//    @PutMapping("/{requestId}")
//    public ResponseEntity<CashCard> putCashCard(@PathVariable Long requestId, @RequestBody CashCard cashCardUpdate,
//                                            Principal principal){
//        CashCard cashCard = cashCardRepository.findByIdAndOwner(requestId, principal.getName());
//        CashCard updatedCashCard = new CashCard(cashCard.id(), cashCardUpdate.amount(), principal.getName());
//        CashCard saved = cashCardRepository.save(updatedCashCard);
//        return ResponseEntity.ok(saved);
//    }
    @PutMapping("/{requestId}")
    public ResponseEntity<CashCard> putCashCard(@PathVariable Long requestId, @RequestBody CashCard cashCardUpdate,
                                                Principal principal){
        CashCard cashCard = findCashCard(requestId, principal);
        if(cashCard != null){
            CashCard updatedCashCard = new CashCard(cashCard.id(), cashCardUpdate.amount(), principal.getName());
            CashCard saved = cashCardRepository.save(updatedCashCard);
            return ResponseEntity.ok(saved);
        }
        return ResponseEntity.notFound().build();
    }

}

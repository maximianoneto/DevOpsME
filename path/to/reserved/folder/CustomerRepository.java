public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByFirstNameContainingOrLastNameContainingAllIgnoreCase(String firstName, String lastName);
}
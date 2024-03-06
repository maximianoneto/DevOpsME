public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> findAllCustomers(Sort sort) {
        return customerRepository.findAll(sort);
    }
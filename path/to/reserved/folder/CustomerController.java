public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public String listCustomers(Model model, @RequestParam(defaultValue = "firstName") String sortBy) {
        List<Customer> customers = customerService.findAllCustomers(Sort.by(sortBy));
        model.addAttribute("customers", customers);
        return "customers";
    }
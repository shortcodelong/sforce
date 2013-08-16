public class StoreFrontController {

	public String selectedCategory {get; set;}
	
	public PageReference save() {
		return null;
	}
	
	public String insertItems {get; set;}
	
	public Cart__c aCart;

	List<DisplayMerchandise> products;	
	List<DisplayMerchandise> shoppingCart = new List<DisplayMerchandise>();

	
	public PageReference addToCart() {
		for(DisplayMerchandise p : products) {
			if(0 < p.qtyToBuy && p.isChecked) {
				shoppingCart.add(p);
			}
		}
		return null;
	}


	public Pagereference makePurchase() {
		aCart = new Cart__c(); 
		insert aCart;
		
		List<Cart_Item__c> itemsList = new List<Cart_Item__c>();
		for (DisplayMerchandise product: shoppingCart) {
			Cart_Item__c ci = new Cart_Item__c(CartItemName__c = product.name, Price__c = product.price, Quantity__c = product.qtyToBuy, Cart__c = aCart.Id);
			itemsList.add(ci);
		}
		insert itemsList;
		return null;
	}


	public String getCartContents() {
		
		if(0 == shoppingCart.size()) {
			return '(empty)';
		}
		String msg = '<ul>\n';
		for(DisplayMerchandise p : shoppingCart) {
			msg += '<li>';
			msg += p.name + ' (x' + p.qtyToBuy + ')';
			msg += ' = ' + '$' + p.totalPriceForEach;		 	
			msg += '</li>\n';			
			//msg += 'TOTAL: ' + '$' + p.qtyToBuy;						
		}
		msg += '</ul>';		
		return msg;
	}
	

	public List<DisplayMerchandise> getProducts() {
		if(products == null) {
			products = new List<DisplayMerchandise>();
			for(Merchandise__c item : [SELECT Id, Name, Description__c, Price__c, Total_Inventory__c
			                           FROM Merchandise__c]) {
				products.add(new DisplayMerchandise(item));
			}			
		}
	return products;
	}
		
				
	// Inner class to hold online store details for item
	public class DisplayMerchandise
	{
		private Merchandise__c merchandise;

			public DisplayMerchandise(Merchandise__c item) {
			this.merchandise = item;
		}

		// Properties for use in the Visualforce view
		public String name {
			get {return Merchandise.Name;}
		}
		
		public String description {
			get {return Merchandise.Description__c;}
		}
		
		public Decimal price {
			get {return Merchandise.Price__c;}
		}
		
		public Boolean inStock {
			get {return (0 < Merchandise.Total_Inventory__c);}
		}
		
		public Decimal totalPriceForEach {
			get {return price * qtyToBuy;} 
				set;}		
		
		public Decimal totalAmount {
			get {return price * qtyToBuy;} 
				set;}				
				
		public Integer qtyToBuy {get; set;}
		
		public Boolean isChecked {get; set;}
	}


	private List<SelectOption> getCategories() {
		List<SelectOption> options = new List<SelectOption>();
		for (Category__c aCategory: [SELECT Id, Name
		                             FROM Category__c]) {
			options.add(new SelectOption(aCategory.Id, aCategory.Name));
		}
		return options;
	}


	public List<SelectOption> categoryOptions {
		get {
			if (categoryOptions == null) {
				categoryOptions = getCategories();
			}
			return categoryOptions;
		} private set;
	}


	public PageReference rerenderTable() {
		products = new List<DisplayMerchandise>();
		List<Id> merchandiseList = new List<Id>();
		for(CategoryMerchandise__c catg : [SELECT Merchandise__c
										   FROM CategoryMerchandise__c
										   WHERE Category__c = :selectedCategory]) {
			merchandiseList.add (catg.Merchandise__c);
		}

		List<Merchandise__c> merchList = [SELECT Id, Name, Description__c, Price__c, Total_Inventory__c
										  FROM Merchandise__c
										  WHERE Id
										  IN :merchandiseList];
		for(Merchandise__c item : merchList) {
			products.add(new DisplayMerchandise(item));
		}
		return null;
	}	
	
}

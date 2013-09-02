public class StoreFrontController {

	public String selectedCategory {get; set;}
	public String insertItems {get; set;}
	public Cart__c aCart;
	public Merchandise__c product {get; set;}
	public CategoryMerchandise__c categMerch {get; set;}
	public String cartId;
	private String inputTextValue = '';
	
	public List<DisplayMerchandise> products;
	public Set<DisplayMerchandise> shoppingCart = new Set<DisplayMerchandise>();
	public List<Data> DataL = new List<Data>(); ////


	public StoreFrontController() {
		product	= new Merchandise__c();
		CartId 	= System.CurrentPageReference().getParameters().get('id');
	}
	
	
	////
	public class Data {
		public Cart__c cart {get; set;}
		public List<Cart_Item__c> items = new List<Cart_Item__c>();
		public Data(Cart__c cart, List<Cart_Item__c> items) {
			this.cart = cart;
			this.items = items;
		}

		public List<Cart_Item__c> getItems() {
			return Items;
		}

	}
	
	
    public List<Data> getDataL() {
    	for (Cart__c cart : [SELECT Id, Name FROM Cart__c WHERE CreatedBy.Name = : UserInfo.getName()]) {
			List<Cart_Item__c> items = [SELECT	Id,
												Name,
												Cart__c,
												Price__c,
												Quantity__c,
												CartItemName__c,
												CreatedBy.Name
										FROM
												Cart_Item__c
								 		WHERE 
								 				CreatedBy.Name = :UserInfo.getName()
								 		AND 
								 				Cart__c = : cart.Id];
			DataL.add(new Data(cart, items));
		} 
		return DataL;
    }


	public List<Cart_Item__c> getpurchList() {
		UserInfo.getUserId();
		return [SELECT
					  Id,
					  Price__c,
					  Quantity__c,
					  CartItemName__c,
					  LastModifiedDate,
					  CreatedBy.Name
				FROM
					  Cart_Item__c
				WHERE 
					  CreatedBy.Name = :UserInfo.getName()];
	}


	public List<Cart__c> getcartList() {
		UserInfo.getUserId();
		return [SELECT
					  Id,
					  Name
				FROM
					  Cart__c
				WHERE
					  CreatedBy.Name = :UserInfo.getName()];
	}

	
	public List<Cart__c> getcurrentItems() {
		UserInfo.getUserId();
		return [SELECT
					  Id,
					  Name
				FROM
					  Cart__c
				WHERE
					  CreatedBy.Name = :UserInfo.getName()
				AND 
					  Id = :cartId];
	}

	public PageReference insertMerch() {
		insert product;		
			CategoryMerchandise__c categMerchForInsert = new CategoryMerchandise__c ();
			categMerchForInsert.Category__c = selectedCategory;
			categMerchForInsert.Merchandise__c = product.id;
		insert categMerchForInsert;
			products = getProducts();

			PageReference thisPageRef = Page.MPCreateProduct;
            thisPageRef.setRedirect(true);
		return thisPageRef;
	}


	public PageReference createCategoryMerchandise() {
		return null;
	}


	public PageReference refreshList() {
		PageReference catalogPageRef = Page.Catalog;
		catalogPageRef.setRedirect(true);
		return catalogPageRef;
	}


	public void uncheckAll() {
		for (DisplayMerchandise p : products) {
			p.isChecked = false;
		}
	}


	public void setInputTextValue(string Value) {
		inputTextValue = Value;
	}


	public PageReference addToCart() {
		for(DisplayMerchandise p : products) {
			if(p.qtyToBuy > 0 && p.isChecked) {
					shoppingCart.add(p);
			}

			if(p.qtyToBuy == 0 && p.isChecked) {
				ApexPages.addmessage(new ApexPages.message(ApexPages.severity.INFO,
				'Error: fill the required field -Quantity to Buy-.'));
			}

		}
		uncheckAll();
		return null;
	}

	public Pagereference makePurchase() {
		aCart = new Cart__c();
		insert aCart;

		List<Cart_Item__c> itemsList = new List<Cart_Item__c>();
		for (DisplayMerchandise product: shoppingCart) {
			Cart_Item__c ci = new Cart_Item__c (
				CartItemName__c = product.name, 
				Price__c 		= product.price, 
				Quantity__c		= product.qtyToBuy, 
				Cart__c 		= aCart.Id );
			itemsList.add(ci);
		}
		insert itemsList;

		PageReference thisPageRef = Page.Catalog;
			thisPageRef.setRedirect(true);
		return thisPageRef;
	}


	public String getCartContents() {
		if(shoppingCart.size() == 0) {
			return '(empty)';
		}
		String msg = '<ul>\n';
		for(DisplayMerchandise p : shoppingCart) {
			msg += '<li>';
			msg += p.name + ' (x' + p.qtyToBuy + ')';
			msg += ' = ' + '$' + p.totalPriceForEach;
			msg += '</li>\n';
		}
		msg += '</ul>';
		return msg;
	}


	public List<DisplayMerchandise> getProducts() {
		if(products == null) {
			products = new List<DisplayMerchandise>();
			for(Merchandise__c item : [SELECT 
											 Id, 
											 Name,
											 Description__c, 
											 Price__c, 
											 Total_Inventory__c
			                           FROM
			                           		 Merchandise__c]) {
			products.add(new DisplayMerchandise(item));}			
		}
	return products;
	}


	// Inner class to hold online store details for item
	public class DisplayMerchandise {

		private Merchandise__c merchandise;
		public Integer qtyToBuy {get; set;}
		public Boolean isChecked {get; set;}

		public DisplayMerchandise(Merchandise__c item) {
			this.merchandise = item;
		}

		// Properties for use in the Visualforce view
		public String ID {
			get {return Merchandise.ID;}
		}

		public String name {
			get {return Merchandise.Name;}
		}

		public String description {
			get {return Merchandise.Description__c;}
		}
		
		public Decimal totalInventory {
			get {return Merchandise.Total_Inventory__c;}
		}

		public Decimal price {
			get {return Merchandise.Price__c;}
		}

		public Boolean inStock {
			get {return (0 < Merchandise.Total_Inventory__c);}
		}

		public Decimal totalPriceForEach {
			get {return price * qtyToBuy;}
				set;
			}

		public Decimal totalAmount {
			get {return price * qtyToBuy;}
				set;
			}

	}


	private List<SelectOption> getCategories() {
		List<SelectOption> options = new List<SelectOption>();
		for (Category__c aCategory: [SELECT
										   Id,
										   Name
		                             FROM
										   Category__c]) {
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
		for(CategoryMerchandise__c catg : [SELECT
												 Id,
												 Merchandise__r.Id,
												 Merchandise__r.Name,
												 Merchandise__r.Description__c,
												 Merchandise__r.Price__c,
												 Merchandise__r.Total_Inventory__c
										   FROM
										   		 CategoryMerchandise__c
										   WHERE
										   		 CategoryMerchandise__c.Category__r.Id = :selectedCategory]) {
		Merchandise__c item = new Merchandise__c (
			Name 		 	   = catg.Merchandise__r.Name,
			Description__c	   = catg.Merchandise__r.Description__c,
			Price__c	 	   = catg.Merchandise__r.Price__c,
			Total_Inventory__c = catg.Merchandise__r.Total_Inventory__c);
				products.add(new DisplayMerchandise(item));
		}
		return null;
	}

}

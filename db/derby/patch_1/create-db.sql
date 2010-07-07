-- IMPORTANT:
--
-- This file was generated by concatenating the three other .sql files together. It can be
-- used for testing, but the separate SQL sequences will be necessary if the Security Master
-- and Position Master need to be installed in different databases.
--
-- Please do not modify it - modify the originals and recreate this using 'ant create-db-sql'.



-- create-db-security.sql: Security Master

create table sec_currency (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_commodityfuturetype (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_bondfuturetype (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_cashrate (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_unit (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_identifier_association (
    id bigint not null,
    security_discriminator varchar(255),
    security_id bigint,
    scheme varchar(255) not null,
    identifier varchar(255) not null,
    validStartDate date,
    validEndDate date,
    primary key (id),
    unique (scheme, identifier, validStartDate, validEndDate)
);

create table sec_exchange (
    id bigint not null,
    name varchar(255) not null unique,
    description varchar(255),
    primary key (id)
);

create table sec_gics (
    id bigint not null,
    name varchar(8) not null unique,
    description varchar(255),
    primary key (id)
);

create table sec_equity (
    id bigint not null,
    effectiveDateTime date not null,
    deleted smallint not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id bigint,
    exchange_id bigint not null,
    companyName varchar(255) not null,
    currency_id bigint not null,
    gicscode_id bigint,
    primary key (id),
    constraint sec_fk_equity2currency foreign key (currency_id) references sec_currency(id),
    constraint sec_fk_equity2exchange foreign key (exchange_id) references sec_exchange(id),
    constraint sec_fk_equity2gics foreign key (gicscode_id) references sec_gics(id)
);

create table sec_option (
    id bigint not null,
    effectiveDateTime date not null,
    deleted smallint not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id bigint,
    option_security_type varchar(32) not null,
    option_type varchar(32) not null,
    strike double precision not null,
    expiry date not null,
    underlying_scheme varchar(255),
    underlying_identifier varchar(255),
    currency_id bigint,
    put_currency_id bigint,
    call_currency_id bigint,
    exchange_id bigint,
    counterparty varchar(255),
    power double,
    margined smallint,
    pointValue double,
    primary key (id),
    constraint sec_fk_option2currency foreign key (currency_id) references sec_currency (id),
    constraint sec_fk_option2putcurrency foreign key (put_currency_id) references sec_currency (id),
    constraint sec_fk_option2callcurrency foreign key (call_currency_id) references sec_currency (id),
    constraint sec_fk_option2exchange foreign key (exchange_id) references sec_exchange (id)
);

create table sec_frequency (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_daycount (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_businessdayconvention (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_issuertype (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_market (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_yieldconvention (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_guaranteetype (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_coupontype (
    id bigint not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_bond (
    id bigint not null,
    effectiveDateTime date not null,
    deleted smallint not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id bigint,
    bond_type varchar(32) not null,
    issuername varchar(255) not null,
    issuertype_id bigint not null,
    issuerdomicile varchar(255) not null,
    market_id bigint not null,
    currency_id bigint not null,
    yieldconvention_id bigint not null,
    guaranteetype_id bigint not null,
    maturity date not null,
    coupontype_id bigint not null,
    couponrate double not null,
    couponfrequency_id bigint not null,
    daycountconvention_id bigint not null,
    businessdayconvention_id bigint not null,
    announcementdate date not null,
    interestaccrualdate date not null,
    settlementdate date not null,
    firstcoupondate date not null,
    issuanceprice double not null,
    totalamountissued double not null,
    minimumamount double not null,
    minimumincrement double not null,
    paramount double not null,
    redemptionvalue double not null,
    primary key (id),
    constraint sec_fk_bond2issuertype foreign key (issuertype_id) references sec_issuertype (id),
    constraint sec_fk_bond2market foreign key (market_id) references sec_market (id),
    constraint sec_fk_bond2currency foreign key (currency_id) references sec_currency (id),
    constraint sec_fk_bond2yieldconvention foreign key (yieldconvention_id) references sec_yieldconvention (id),
    constraint sec_fk_bond2guaranteetype foreign key (guaranteetype_id) references sec_guaranteetype (id),
    constraint sec_fk_bond2coupontype foreign key (coupontype_id) references sec_coupontype (id),
    constraint sec_fk_bond2frequency foreign key (couponfrequency_id) references sec_frequency (id),
    constraint sec_fk_bond2daycount foreign key (daycountconvention_id) references sec_daycount (id),
    constraint sec_fk_bond2businessdayconvention foreign key (businessdayconvention_id) references sec_businessdayconvention (id)
);

create table sec_future (
    id bigint not null,
    effectiveDateTime date not null,
    deleted smallint not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id bigint,
    future_type varchar(32) not null,
    expiry date not null,
    tradingexchange_id bigint not null,
    settlementexchange_id bigint not null,
    currency1_id bigint,
    currency2_id bigint,
    currency3_id bigint,
    bondtype_id bigint,
    commoditytype_id bigint,
    cashratetype_id bigint,
    unitname_id bigint,
    unitnumber double precision,
    underlying_scheme varchar(255),
    underlying_identifier varchar(255), 
    primary key (id),
    constraint sec_fk_future2exchange1 foreign key (tradingexchange_id) references sec_exchange (id),
    constraint sec_fk_future2exchange2 foreign key (settlementexchange_id) references sec_exchange (id),
    constraint sec_fk_future2currency1 foreign key (currency1_id) references sec_currency (id),
    constraint sec_fk_future2currency2 foreign key (currency2_id) references sec_currency (id),
    constraint sec_fk_future2currency3 foreign key (currency3_id) references sec_currency (id),
    constraint sec_fk_future2bondfuturetype foreign key (bondtype_id) references sec_bondfuturetype (id),
    constraint sec_fk_future2commodityfuturetype foreign key (commoditytype_id) references sec_commodityfuturetype (id),
    constraint sec_fk_future2cashrate foreign key (cashratetype_id) references sec_cashrate (id),
    constraint sec_fk_future2unit foreign key (unitname_id) references sec_unit (id)
);

create table sec_futurebundle (
    id bigint not null,
    future_id bigint not null,
    startDate date,
    endDate date,
    conversionFactor double not null,
    primary key (id),
    constraint sec_fk_futurebundle2future foreign key (future_id) references sec_future (id)
);

create table sec_futurebundleidentifier (
    bundle_id bigint not null,
    scheme varchar(255) not null,
    identifier varchar(255) not null,
    primary key (bundle_id, scheme, identifier),
    constraint sec_fk_futurebundleidentifier2futurebundle foreign key (bundle_id) references sec_futurebundle (id)
);
create table pos_portfolio (
    oid bigint not null,
    version bigint not null,
    status char(1) not null,
    start_instant timestamp,
    end_instant timestamp,
    name varchar(255) not null,
    primary key (oid, version)
);

create table pos_node (
    portfolio_oid bigint not null,
    oid bigint not null,
    start_version bigint not null,
    end_version bigint not null,
    name varchar(255),
    primary key (oid, start_version),
    constraint pos_fk_node2portfolio foreign key (portfolio_oid, start_version) references pos_portfolio (oid, version)
);

create table pos_nodetree (
    portfolio_oid bigint not null,
    parent_node_oid bigint,
    node_oid bigint not null,
    start_version bigint not null,
    end_version bigint not null,
    left_id bigint not null,
    right_id bigint not null,
    primary key (node_oid, start_version),
    constraint pos_fk_nodetree2portfolio foreign key (portfolio_oid, start_version) references pos_portfolio (oid, version)
);
-- portfolio_oid is an optimization
-- parent_node_oid is an optimization (left_id/right_id hold all the tree structure)

create table pos_position (
    portfolio_oid bigint not null,
    node_oid bigint not null,
    oid bigint not null,
    start_version bigint not null,
    end_version bigint not null,
    quantity decimal not null,
    primary key (oid, start_version),
    constraint pos_fk_position2portfolio foreign key (portfolio_oid, start_version) references pos_portfolio (oid, version)
);
-- portfolio_oid is an optimization

create table pos_securitykey (
    position_oid bigint not null,
    position_version bigint not null,
    id_scheme varchar(255) not null,
    id_value varchar(255) not null,
    primary key (position_oid, position_version, id_scheme, id_value),
    constraint pos_fk_securitykey2position foreign key (position_oid, position_version) references pos_position (oid, start_version)
);
-- pos_securitykey is fully dependent of pos_position
-- pos_securitykey.position_version = pos_position.start_version

    create table hibernate_sequence (
         next_val bigint
    );

    insert into hibernate_sequence values ( 1 );

-------------------------------------
-- Static data
-------------------------------------

create table rsk_observation_time (
    id int not null,
    label varchar(255) not null,                -- LDN_CLOSE
    
    primary key (id),
    
    unique (label)
);

create table rsk_observation_datetime (
	id int not null,
	date_part date not null,  
	time_part time,						-- null if time of LDN_CLOSE not fixed yet
	observation_time_id int not null,    		  
	
	primary key (id),
	
	constraint fk_rsk_obs_datetime2obs_time
	    foreign key (observation_time_id) references rsk_observation_time (id),
	    
	constraint chk_rsk_obs_datetime check 
	    (time_part is not null or observation_time_id is not null), 
	
	unique (date_part, observation_time_id)
);

create table rsk_compute_host (
	id int not null,
	host_name varchar(255) not null,
	
	primary key (id),
	
	unique (host_name)
);

create table rsk_compute_node (
	id int not null,
	config_oid varchar(255) not null,
	config_version int not null,
	compute_host_id int not null,
	node_name varchar(255) not null,
	
	primary key (id),
	
	constraint fk_rsk_cmpt_node2cmpt_host
	    foreign key (compute_host_id) references rsk_compute_host (id),
	    
	unique (config_oid, config_version)
);

create table rsk_opengamma_version (
	id int not null,
	version varchar(255) not null, 
	hash varchar(255) not null,
	
	primary key (id),
	
	unique (version, hash)
);

-- DBTOOLDONOTCLEAR
create table rsk_computation_target_type ( 
	id int not null,	 	            
    name varchar(255) not null,
    
    primary key (id),
    
    constraint chk_rsk_cmpt_target_type check
        ((id = 0 and name = 'PORTFOLIO_NODE') or
         (id = 1 and name = 'POSITION') or 
         (id = 2 and name = 'SECURITY') or
         (id = 3 and name = 'PRIMITIVE'))
);

insert into rsk_computation_target_type (id, name) values (0, 'PORTFOLIO_NODE');
insert into rsk_computation_target_type (id, name) values (1, 'POSITION');
insert into rsk_computation_target_type (id, name) values (2, 'SECURITY');
insert into rsk_computation_target_type (id, name) values (3, 'PRIMITIVE');

create table rsk_computation_target (
	id int not null,
	type_id int not null,
	id_scheme varchar(255) not null,
	id_value varchar(255) not null,
	
	primary key (id),
	
	constraint fk_rsk_cmpt_target2tgt_type 
	    foreign key (type_id) references rsk_computation_target_type (id),
	    
	unique (type_id, id_scheme, id_value)
);

-------------------------------------
-- LiveData inputs
-------------------------------------

create table rsk_live_data_field (
	id int not null,
	name varchar(255) not null,
	
	primary key (id),
	
	unique(name)
);

create table rsk_live_data_snapshot (
	id int not null,
	observation_datetime_id int not null,
	complete smallint not null,
	
	primary key (id),
	
	constraint fk_rsk_lv_data_snap2ob_dttime
	    foreign key (observation_datetime_id) references rsk_observation_datetime (id),
	    
	unique (observation_datetime_id)
);

create table rsk_live_data_snapshot_entry (
	id bigint not null,
	snapshot_id int not null,
	computation_target_id int not null,
	field_id int not null,
	value double precision,
	
	primary key (id),
	
	constraint fk_rsk_snpsht_entry2snpsht
		foreign key (snapshot_id) references rsk_live_data_snapshot (id),
	constraint fk_rsk_spsht_entry2cmp_target
	    foreign key (computation_target_id) references rsk_computation_target (id),
	    
	unique (snapshot_id, computation_target_id, field_id) 	
);

-------------------------------------
-- Risk run
-------------------------------------

create table rsk_run (
	id int not null,
	opengamma_version_id int not null,
	master_process_host_id int not null,    -- machine where 'master' batch process was started
    run_reason varchar(255) not null,       -- 15 June main overnight batch run
    run_time_id int not null,
    valuation_time timestamp not null,	 	-- 15 June 2010 17:00:00 - 'T'
    view_oid varchar(255) not null,
    view_version int not null,
    live_data_snapshot_id int not null,
    create_instant timestamp not null,
    start_instant timestamp not null,       -- can be different from create_instant if is run is restarted
    end_instant	timestamp,
    complete smallint not null,
    
    primary key (id),
    
    constraint fk_rsk_run2opengamma_version
        foreign key (opengamma_version_id) references rsk_opengamma_version (id),
    constraint fk_rsk_run2compute_host
        foreign key (master_process_host_id) references rsk_compute_host (id),
    constraint fk_rsk_run2obs_datetime
        foreign key (run_time_id) references rsk_observation_datetime (id),
    constraint fk_rsk_run2live_data_snapshot
        foreign key (live_data_snapshot_id) references rsk_live_data_snapshot (id)
);

create table rsk_calculation_configuration (
	id int not null,
	run_id int not null,
	name varchar(255) not null,
	
	primary key (id),
	
	constraint fk_rsk_calc_conf2run
	    foreign key (run_id) references rsk_run (id),
	
	unique (run_id, name)
);

-- Properties should be filled once only. If already there, use existing value.
--
-- Example properties:
-- 	- PositionMasterTime = 20100615170000
--  - GlobalRandomSeed = 54321
create table rsk_run_property (		
	id int not null,
	run_id int not null,
	property_key varchar(255) not null,
	property_value varchar(2000) not null,		    -- varchar(255) not enough
	
	primary key (id),

	constraint fk_rsk_run_property2run 
	    foreign key (run_id) references rsk_run (id)
);

-------------------------------------
-- Risk
-------------------------------------

create table rsk_value_name (
    id int not null,
    name varchar(255) not null,
    
    primary key (id),
    
    unique (name)
);

create table rsk_value (
    id bigint not null,
    calculation_configuration_id int not null,
    value_name_id int not null,                 
    computation_target_id int not null,        
    run_id int not null,             	       -- shortcut
    value double precision not null,
    eval_instant timestamp not null,
    compute_node_id int not null,
    
    primary key (id),
    
    -- performance implications of these constraints?
    constraint fk_rsk_value2calc_conf
        foreign key (calculation_configuration_id) references rsk_calculation_configuration (id),
    constraint fk_rsk_value2run 
        foreign key (run_id) references rsk_run (id),
    constraint fk_rsk_value2value_name
        foreign key (value_name_id) references rsk_value_name (id),
    constraint fk_rsk_value2comp_target
        foreign key (computation_target_id) references rsk_computation_target (id),
    constraint fk_rsk_value2compute_node
        foreign key (compute_node_id) references rsk_compute_node (id),
        
    unique (calculation_configuration_id, value_name_id, computation_target_id)
);

create table rsk_compute_failure (			
    id bigint not null,
    function_id varchar(255) not null,
    function_name varchar(255) not null,
    exception_class varchar(255) not null,
    stack_trace varchar(2000) not null,         -- first 2000 chars. not including msg
    
    primary key (id),
    
    unique (function_id, function_name, exception_class, stack_trace)
);

-- how to aggregate risk failures?
create table rsk_failure (			
    id bigint not null,
    calculation_configuration_id int not null,
    value_name_id int not null,                 
    computation_target_id int not null,
    run_id int not null,             	       -- shortcut
    
    primary key (id),
    
    constraint fk_rsk_failure2calc_conf 
        foreign key (calculation_configuration_id) references rsk_calculation_configuration (id),
    constraint fk_rsk_failure2run 
        foreign key (run_id) references rsk_run (id),
    constraint fk_rsk_failure2value_name
        foreign key (value_name_id) references rsk_value_name (id),
    constraint fk_rsk_failure2com_target
        foreign key (computation_target_id) references rsk_computation_target (id),
        
    unique (calculation_configuration_id, value_name_id, computation_target_id)
);    

create table rsk_failure_reason (
   id bigint not null,
   rsk_failure_id bigint not null,
   compute_failure_id bigint not null,
   eval_instant timestamp not null,
   compute_node_id int not null,
   exception_msg varchar(255) not null,                  
   
   primary key (id),
   
   constraint fk_rsk_fail_reason2failure
       foreign key (rsk_failure_id) references rsk_failure (id),
   constraint fk_rsk_fail_reason2cmpt_fail
       foreign key (compute_failure_id) references rsk_compute_failure (id),
   constraint fk_rsk_fail_reason2node
       foreign key (compute_node_id) references rsk_compute_node (id) 
);

